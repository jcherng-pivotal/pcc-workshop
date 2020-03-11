package io.pivotal.workshop.pcc.sizer.generator;

import com.github.javafaker.Faker;
import io.pivotal.workshop.pcc.entity.Dummy;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DummyJavaFakerGenerator implements Generator<Dummy> {

    private final Random random;
    private final Faker faker;

    public DummyJavaFakerGenerator() {
        random = new Random(0);
        faker = Faker.instance(random);
    }

    @Override
    public Dummy getObject(int idSeed) {
        return Dummy.builder()
                .id("C" + idSeed)
                .aBoolean(faker.bool().bool())
                .aShort((short) faker.number().numberBetween(0, 100))
                .anInt(faker.number().randomDigit())
                .aLong(faker.number().randomNumber())
                .build();
    }
}
