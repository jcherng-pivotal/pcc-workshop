package io.pivotal.workshop.pcc.client.config;

import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;

@CacheServerApplication
@EnableLocator
@EnableManager
@Profile("!cloud")
public class ExtendedGemFireConfig {

}