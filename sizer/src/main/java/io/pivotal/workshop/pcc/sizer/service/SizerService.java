package io.pivotal.workshop.pcc.sizer.service;

import io.pivotal.workshop.pcc.sizer.generator.Generator;
import io.pivotal.workshop.pcc.sizer.generator.GeneratorType;
import io.pivotal.workshop.pcc.sizer.histogram.LocalServerHistogramer;
import io.pivotal.workshop.pcc.sizer.repository.RepositoryType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        List<Long> resultList = new ArrayList<>();
        List<Long> memoryList = new ArrayList<>();


        // need to check the math
        for (int i = 0; i < 5; i++) {
            log.info("calculation #" + (i + 1));
            int numberOfRecords = (int) Math.pow(2, i) * 500;
            LocalServerHistogramer localServerHistogramer =
                    new LocalServerHistogramer(generator, repository, numberOfRecords);
            long memory = localServerHistogramer.calculate().getTotalMemory();

            if (i > 0) {
                int previousNumberOfRecords = (int) Math.pow(2, i - 1) * 500;
                long previousMemory = memoryList.get(i - 1);
                resultList.add(solve(previousNumberOfRecords, previousMemory, numberOfRecords, memory, expectedNumberOfRecords));
            }
            memoryList.add(memory);
        }

        long bytes = resultList.stream().min(Comparator.comparing( aLong -> aLong )).get();

        return getResult(bytes);
    }

    private String getResult(long bytes) {
        return "total memory - SI: " + humanReadableByteCountSI(bytes) +
                " BIN: " + humanReadableByteCountBin(bytes);
    }

    /***
     * y = ax + b
     * a = (y2 - y1)/(x2 - x1)
     * b = y2 - (a * x2)
     * @param x1 number of records from set 1
     * @param y1 number of bytes from set 1
     * @param x2 number of records from set 2
     * @param y2 number of bytes from set 2
     * @param expectedNumberOfRecords expected number of records
     * @return
     */
    private long solve(int x1, long y1, int x2, long y2, int expectedNumberOfRecords) {
        long a = (y2 - y1) / (x2 - x1);
        long b = y2 - (a * x2);

        return (a * expectedNumberOfRecords) + b;
    }
}
