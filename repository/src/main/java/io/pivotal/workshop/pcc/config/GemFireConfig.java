package io.pivotal.workshop.pcc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.geode.config.annotation.EnableClusterAware;

@Configuration
@EnableEntityDefinedRegions(basePackages = "io.pivotal.workshop.pcc.entity")
@EnableGemfireRepositories(basePackages = "io.pivotal.workshop.pcc.repository")
@EnableClusterAware
public class GemFireConfig {

}