package io.pivotal.workshop.pcc.sizer.generator;

import io.pivotal.workshop.pcc.entity.Customer;
import org.jeasy.random.EasyRandom;
import org.springframework.stereotype.Component;

@Component
public class CustomerEasyRandomGenerator implements Generator<Customer> {

    private final EasyRandom easyRandom;

    public CustomerEasyRandomGenerator() {
        easyRandom = new EasyRandom();
    }

    @Override
    public Customer getObject(int idSeed) {
        return easyRandom.nextObject(Customer.class)
                .toBuilder().id("C" + idSeed).build();
    }
}
