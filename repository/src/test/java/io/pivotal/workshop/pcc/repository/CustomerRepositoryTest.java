package io.pivotal.workshop.pcc.repository;


import io.pivotal.workshop.pcc.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testAdd() {
        Customer customer = Customer
                .builder()
                .id("id1")
                .build();
        Customer actual = customerRepository.save(customer);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo("id1");
    }

    @Test
    void testGet() {
        Customer customer = Customer
                .builder()
                .id("id2")
                .build();
        Customer actual = customerRepository.save(customer);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo("id2");

        actual = customerRepository.findById("id2").get();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo("id2");
    }
}