package io.pivotal.workshop.pcc.sizer.generator;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Generator<T> {

    AtomicInteger counter = new AtomicInteger();

    default Set<T> getCustomers(int numberOfRecords) {
        return IntStream.range(0, numberOfRecords)
                .mapToObj(value -> getObject(counter.getAndIncrement()))
                .collect(Collectors.toSet());
    }

    T getObject(int idSeed);
}
