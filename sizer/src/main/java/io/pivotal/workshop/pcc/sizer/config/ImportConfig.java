package io.pivotal.workshop.pcc.sizer.config;

import io.pivotal.workshop.pcc.config.GemFireConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * The {@link ImportConfig} class is a Spring {@link Configuration @Configuration} class
 * that enables import of configurations from other modules
 *
 * @see Configuration
 * @see Import
 */
@Configuration
@Import(GemFireConfig.class)
public class ImportConfig {

}
