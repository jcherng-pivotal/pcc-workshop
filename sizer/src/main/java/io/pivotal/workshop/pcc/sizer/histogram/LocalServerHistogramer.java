package io.pivotal.workshop.pcc.sizer.histogram;

import com.jerolba.jmnemohistosyne.HistogramEntry;
import com.jerolba.jmnemohistosyne.MemoryHistogram;
import io.pivotal.workshop.pcc.sizer.generator.Generator;
import io.pivotal.workshop.pcc.sizer.repository.SpecialGemfireRepository;
import io.pivotal.workshop.pcc.sizer.util.GemFireScriptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.geode.internal.cache.RegionEntry;
import org.reflections.Reflections;
import org.springframework.data.gemfire.repository.GemfireRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.pivotal.workshop.pcc.sizer.histogram.MemoryHistogramType.DECREASE;
import static io.pivotal.workshop.pcc.sizer.histogram.MemoryHistogramType.INCREASE;
import static java.lang.Long.parseLong;

@Slf4j
public class LocalServerHistogramer {
    private final Set<String> specialClasses;
    private final Set<String> excludedClasses;
    private final SpecialGemfireRepository repository;
    private final Set<?> records;

    public LocalServerHistogramer(Generator generator, GemfireRepository repository, int numberOfRecords) {
        this.repository = SpecialGemfireRepository.getInstance(repository);
        this.records = generator.getCustomers(numberOfRecords);

        // data related classes
        // do we need to include the classes from the `org.apache.geode.internal.cache.entries`?
        Reflections reflections = new Reflections("org.apache.geode.internal.cache.entries");
        specialClasses = reflections
                .getSubTypesOf(RegionEntry.class)
                .stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.toSet());

        specialClasses.add("[B");
        specialClasses.add("org.apache.geode.internal.cache.PreferBytesCachedDeserializable");
        specialClasses.add("org.apache.geode.internal.cache.DiskId$PersistenceWithIntOffset");

        excludedClasses = new HashSet<>();

        stabilize();
    }

    private void resetGemFire() {
        try {
            GemFireScriptUtil.recreateRegions();
            GemFireScriptUtil.recreateIndexes();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private MemoryHistogram getBaseMemoryHistogram() {
        resetGemFire();
        return createHistogram();
    }

    private void stabilize() {
        log.info("stabilizing classes for memory calculation");
        resetGemFire();
        IntStream.range(0, 5).forEach(value -> {
            load();
            unload();
        });
        log.info("stabilized classes for memory calculation");
    }

    private MemoryHistogram load() {
        MemoryHistogram reference = createHistogram();
        repository.saveAll(records);
        return stabilizeClasses(reference, INCREASE);
    }

    private MemoryHistogram unload() {
        MemoryHistogram reference = createHistogram();
        repository.deleteAll();
        return stabilizeClasses(reference, DECREASE);
    }

    public MemoryHistogram calculate() {
        MemoryHistogram result = null;
        log.info("loading data and calculating memory consumption");
        while (result == null) {
            try {
                MemoryHistogram base = getBaseMemoryHistogram();
                MemoryHistogram change = load();
                result = diff(base, change);
            } catch (CalculationException e) {
                //NOOP
                log.error("wrong calculation - retry!");
            }
        }
        return result;
    }

    private MemoryHistogram diff(MemoryHistogram base, MemoryHistogram change) {
        MemoryHistogram memoryHistogram = new MemoryHistogram();
        change.stream().map(histogramEntry -> {
            HistogramEntry baseHistogramEntry = base.get(histogramEntry.getClassName());
            if (baseHistogramEntry != null && baseHistogramEntry.getSize() <= histogramEntry.getSize()) {
                long instances = histogramEntry.getInstances() - baseHistogramEntry.getInstances();
                long size = histogramEntry.getSize() - baseHistogramEntry.getSize();
                return new HistogramEntry(histogramEntry.getClassName(), instances, size);
            } else if (baseHistogramEntry == null) {
                return histogramEntry;
            } else {
                throw new CalculationException();
            }
        }).sorted(Comparator.comparingLong(value -> value.getSize()))
                .collect(Collectors.toList()).forEach(memoryHistogram::add);

        return memoryHistogram;
    }

    private MemoryHistogram stabilizeClasses(MemoryHistogram reference, MemoryHistogramType memoryHistogramType) {
        MemoryHistogram result = new MemoryHistogram();

        MemoryHistogram current = createHistogram();
        current.forEach(histogramEntry -> {
            String className = histogramEntry.getClassName();
            HistogramEntry referenceHistogramEntry = reference.get(className);

            if (specialClasses.contains(className) || (referenceHistogramEntry == null && memoryHistogramType == INCREASE) ||
                    (referenceHistogramEntry != null && (
                            histogramEntry.getSize() > referenceHistogramEntry.getSize() && memoryHistogramType == INCREASE ||
                                    histogramEntry.getSize() < referenceHistogramEntry.getSize() && memoryHistogramType == DECREASE
                    ))) {
                if (!excludedClasses.contains(className)) {
                    result.add(histogramEntry);
                }
            } else {
                excludedClasses.add(className);
            }
        });

        return result;
    }

    private MemoryHistogram createHistogram() {
        MemoryHistogram histogram = new MemoryHistogram();
        List<String> commandOutput = runJcmd();
        commandOutput.forEach(s -> {
            String[] columns = s.trim().split("\\s+");
            if (columns.length == 4) {
                histogram.add(new HistogramEntry(columns[3], parseLong(columns[1]), parseLong(columns[2])));
            }
        });

        return histogram;
    }

    /**
     * Executes the jcmd command with GC.class_histogram parameter. It's expected to
     * be in path.
     */
    private List<String> runJcmd() {
        try {
            Runtime.getRuntime().exec("gfsh -e \"connect\" -e \"list members\"");
            String pid = getServerLuncherPID();
            Process p = Runtime.getRuntime().exec("jcmd " + pid + " GC.class_histogram");
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                return input.lines().collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getServerLuncherPID() throws IOException {
        Process p = Runtime.getRuntime().exec("jps -l");
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(
                p.getInputStream(), StandardCharsets.UTF_8));

        String pid = null;
        while ((line = in.readLine()) != null) {
            String[] javaProcess = line.split(" ");
            if (javaProcess.length > 1 && javaProcess[1].endsWith("ServerLauncher")) {
                pid = javaProcess[0];
            }
        }

        return pid;
    }

    private class CalculationException extends RuntimeException {

    }
}