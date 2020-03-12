package io.pivotal.workshop.pcc.sizer.service;

import edu.princeton.cs.algs4.LinearRegression;
import io.pivotal.workshop.pcc.sizer.generator.Generator;
import io.pivotal.workshop.pcc.sizer.generator.GeneratorType;
import io.pivotal.workshop.pcc.sizer.histogram.LocalServerHistogramer;
import io.pivotal.workshop.pcc.sizer.repository.RepositoryType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class SizerService {

    private final ApplicationContext applicationContext;

    public static String humanReadableByteCountSI(long bytes) {
        String s = bytes < 0 ? "-" : "";
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1000L ? bytes + " B"
                : b < 999_950L ? String.format("%s%.1f kB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f MB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f GB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f TB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f PB", s, b / 1e3)
                : String.format("%s%.1f EB", s, b / 1e6);
    }

    public static String humanReadableByteCountBin(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? bytes + " B"
                : b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KiB", bytes / 0x1p10)
                : b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MiB", bytes / 0x1p20)
                : b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GiB", bytes / 0x1p30)
                : b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TiB", bytes / 0x1p40)
                : b <= 0xfffccccccccccccL ? String.format("%.1f PiB", (bytes >> 10) / 0x1p40)
                : String.format("%.1f EiB", (bytes >> 20) / 0x1p40);
    }

    public void load(GeneratorType generatorType,
                     RepositoryType repositoryType,
                     int numberOfRecords) {
        Generator generator = (Generator) applicationContext
                .getBean(generatorType.getBeanClass());
        GemfireRepository repository = (GemfireRepository) applicationContext
                .getBean(repositoryType.getBeanClass());

        Set<?> objects = generator.getCustomers(numberOfRecords);
        repository.saveAll(objects);
    }

    public String accurate(GeneratorType generatorType,
                           RepositoryType repositoryType,
                           int numberOfRecords) {
        Generator generator = (Generator) applicationContext
                .getBean(generatorType.getBeanClass());
        GemfireRepository repository = (GemfireRepository) applicationContext
                .getBean(repositoryType.getBeanClass());

        long bytes = new LocalServerHistogramer(generator, repository, numberOfRecords)
                .calculate().getTotalMemory();

        return getResult(bytes);
    }

    public String estimate(GeneratorType generatorType,
                           RepositoryType repositoryType,
                           int expectedNumberOfRecords) {
        Generator generator = (Generator) applicationContext
                .getBean(generatorType.getBeanClass());
        GemfireRepository repository = (GemfireRepository) applicationContext
                .getBean(repositoryType.getBeanClass());

        int round = 5;
        double[] recordCounts = new double[round];
        double[] memorySizes = new double[round];

        for (int i = 0; i < round; i++) {
            log.info("calculation #" + (i + 1));
            int numberOfRecords = (int) Math.pow(2, i) * 500;
            LocalServerHistogramer localServerHistogramer =
                    new LocalServerHistogramer(generator, repository, numberOfRecords);
            long memorySize = localServerHistogramer.calculate().getTotalMemory();

            recordCounts[i] = (double) numberOfRecords;
            memorySizes[i] = (double) memorySize;
        }

        LinearRegression linearRegression = new LinearRegression(recordCounts, memorySizes);
        long bytes = (long) linearRegression.predict(expectedNumberOfRecords);
        return getResult(bytes);
    }

    private String getResult(long bytes) {
        return "total memory - SI: " + humanReadableByteCountSI(bytes) +
                " BIN: " + humanReadableByteCountBin(bytes);
    }
}
