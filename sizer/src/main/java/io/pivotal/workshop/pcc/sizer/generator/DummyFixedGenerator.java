package io.pivotal.workshop.pcc.sizer.generator;

import io.pivotal.workshop.pcc.entity.Dummy;
import org.springframework.stereotype.Component;

@Component
public class DummyFixedGenerator implements Generator<Dummy> {

    @Override
    public Dummy getObject(int idSeed) {
        return Dummy.builder()
                .id("C" + idSeed)
                .aBoolean(true)
                .aShort((short) 100)
                .anInt(100)
                .aLong(100L)
                .build();
    }
}
