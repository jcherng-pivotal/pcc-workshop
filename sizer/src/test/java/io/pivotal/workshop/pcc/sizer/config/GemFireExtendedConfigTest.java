package io.pivotal.workshop.pcc.sizer.config;

import org.apache.geode.cache.GemFireCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GemFireExtendedConfigTest {

    @Autowired
    private GemFireCache cache;

    @Test
    void testGemFireCache() {
        assertThat(cache).isNotNull();
    }
}