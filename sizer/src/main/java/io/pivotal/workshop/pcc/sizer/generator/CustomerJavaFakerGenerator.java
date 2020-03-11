package io.pivotal.workshop.pcc.sizer.generator;

import com.github.javafaker.Faker;
import io.pivotal.workshop.pcc.entity.Customer;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CustomerJavaFakerGenerator implements Generator<Customer> {

    private final Random random;
    private final Faker faker;

    public CustomerJavaFakerGenerator() {
        random = new Random(0);
        faker = Faker.instance(random);
    }

    @Override
    public Customer getObject(int idSeed) {
        return Customer.builder()
                .id("C" + idSeed)
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .dob(faker.number().numberBetween(19000101, 20200101))
                .build();
    }
}
