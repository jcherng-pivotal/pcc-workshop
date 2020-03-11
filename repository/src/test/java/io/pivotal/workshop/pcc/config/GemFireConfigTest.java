package io.pivotal.workshop.pcc.config;

import io.pivotal.workshop.pcc.entity.Customer;
import io.pivotal.workshop.pcc.repository.CustomerRepository;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GemFireConfigTest {

    @Autowired
    private GemFireCache cache;

    @Autowired
    @Qualifier("customer")
    private Region<String, Customer> customerRegion;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void contextLoads() {
        assertThat(cache).isNotNull();
        assertThat(customerRegion).isNotNull();
        assertThat(customerRepository).isNotNull();
    }
}