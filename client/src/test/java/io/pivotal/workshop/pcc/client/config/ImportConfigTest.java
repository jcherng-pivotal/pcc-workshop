package io.pivotal.workshop.pcc.client.config;

import io.pivotal.workshop.pcc.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ImportConfigTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void testImportedComponents() {
        assertThat(customerRepository).isNotNull();
    }
}