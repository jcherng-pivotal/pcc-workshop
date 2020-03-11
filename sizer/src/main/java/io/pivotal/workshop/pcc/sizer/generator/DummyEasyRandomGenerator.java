package io.pivotal.workshop.pcc.sizer.generator;

import io.pivotal.workshop.pcc.entity.Dummy;
import org.jeasy.random.EasyRandom;
import org.springframework.stereotype.Component;

@Component
public class DummyEasyRandomGenerator implements Generator<Dummy> {

    private final EasyRandom easyRandom;

    public DummyEasyRandomGenerator() {
        easyRandom = new EasyRandom();
    }

    @Override
    public Dummy getObject(int idSeed) {
        return easyRandom.nextObject(Dummy.class)
                .toBuilder().id("C" + idSeed).build();
    }
}
