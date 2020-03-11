package io.pivotal.workshop.pcc.sizer;

import io.pivotal.workshop.pcc.sizer.util.GemFireScriptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@EnableSwagger2
@SpringBootApplication
public class SizerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SizerApplication.class);

        application.addListeners((ApplicationListener<ApplicationEvent>) event -> {
            try {
                if (event instanceof ApplicationEnvironmentPreparedEvent) {
                    GemFireScriptUtil.startGemFire();
                }
                if (event instanceof ApplicationFailedEvent || event instanceof ContextClosedEvent) {
                    GemFireScriptUtil.shutdownGemFire();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        application.run(args);
    }
}