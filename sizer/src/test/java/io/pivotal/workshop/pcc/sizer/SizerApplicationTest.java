package io.pivotal.workshop.pcc.sizer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SizerApplicationTest {
    @Autowired
    private ApplicationContext context;

    @Test
    void testApplicationContext() {
        assertThat(context).isNotNull();
    }
}