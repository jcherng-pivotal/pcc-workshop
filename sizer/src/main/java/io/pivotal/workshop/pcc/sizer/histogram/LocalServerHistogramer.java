package io.pivotal.workshop.pcc.sizer.histogram;

import com.jerolba.jmnemohistosyne.HistogramEntry;
import com.jerolba.jmnemohistosyne.MemoryHistogram;
import io.pivotal.workshop.pcc.sizer.generator.Generator;
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
    private final Set<String> includedClasses;
    private final Set<String> excludedClasses;
    private final Generator generator;
    private final GemfireRepository repository;
    private final int numberOfRecords;

    public LocalServerHistogramer(Generator generator, GemfireRepository repository, int numberOfRecords) {
        this.generator = generator;
        this.repository = repository;
        this.numberOfRecords = numberOfRecords;
        // data related classes
        Reflections reflections = new Reflections("org.apache.geode.internal.cache.entries");

        specialClasses = reflections
                .getSubTypesOf(RegionEntry.class)
                .stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.toSet());
        specialClasses.add("[B");
        specialClasses.add("org.apache.geode.internal.cache.PreferBytesCachedDeserializable");
        specialClasses.add("org.apache.geode.internal.cache.DiskId$PersistenceWithIntOffset");

        includedClasses = new HashSet<>();
        excludedClasses = new HashSet<>();
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
        resetGemFire();
        Set<?> objects = generator.getCustomers(1000);
        repository.saveAll(objects);
        repository.deleteAll(objects);

        IntStream.range(0, 5).forEach(value -> {
            load((value + 1) * 1000);
        });
        unload();
    }

    private MemoryHistogram load(int numberOfRecords) {
        MemoryHistogram reference = createHistogram();
        Set<?> objects = generator.getCustomers(numberOfRecords);
        repository.saveAll(objects);
        return stabilizeClasses(reference, INCREASE);
    }

    private MemoryHistogram unload() {
        MemoryHistogram reference = createHistogram();
        repository.deleteAll(repository.findAll());
        return stabilizeClasses(reference, DECREASE);
    }

    public MemoryHistogram calculate() {
        stabilize();

        MemoryHistogram base = getBaseMemoryHistogram();
        MemoryHistogram change = load(numberOfRecords);
        unload();
        return diff(base, change);
    }

    private MemoryHistogram diff(MemoryHistogram base, MemoryHistogram change) {
        MemoryHistogram memoryHistogram = new MemoryHistogram();
        includedClasses.stream().map(s -> {
            HistogramEntry baseHistogramEntry = base.get(s);
            HistogramEntry changeHistogramEntry = change.get(s);
            if (baseHistogramEntry != null && changeHistogramEntry != null
                    && baseHistogramEntry.getInstances() < changeHistogramEntry.getInstances()) {
                long instances = changeHistogramEntry.getInstances() - baseHistogramEntry.getInstances();
                long size = changeHistogramEntry.getSize() - baseHistogramEntry.getSize();
                return new HistogramEntry(s, instances, size);
            } else if(baseHistogramEntry == null && changeHistogramEntry != null){
                return changeHistogramEntry;
            } else {
                    throw new RuntimeException("wrong calculation!!");
            }
        }).sorted(Comparator.comparingLong(value -> value.getSize()))
                .collect(Collectors.toList()).forEach(histogramEntry -> memoryHistogram.add(histogramEntry));

        return memoryHistogram;
    }

    private MemoryHistogram stabilizeClasses(MemoryHistogram reference, MemoryHistogramType memoryHistogramType) {
        MemoryHistogram result = new MemoryHistogram();

        MemoryHistogram current = createHistogram();
        current.forEach(histogramEntry -> {
            String className = histogramEntry.getClassName();
            HistogramEntry referenceHistogramEntry = reference.get(className);

            if (specialClasses.contains(className)) {
                result.add(histogramEntry);
            }

            if ((referenceHistogramEntry == null && memoryHistogramType == INCREASE) ||
                    (referenceHistogramEntry != null && (
                            histogramEntry.getInstances() > referenceHistogramEntry.getInstances() && memoryHistogramType == INCREASE ||
                                    histogramEntry.getInstances() < referenceHistogramEntry.getInstances() && memoryHistogramType == DECREASE
                    ))) {
                if (!excludedClasses.contains(className)) {
                    includedClasses.add(className);
                    result.add(histogramEntry);
                }
            } else {
                excludedClasses.add(className);
                includedClasses.remove(className);
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
}