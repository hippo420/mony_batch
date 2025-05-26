package app.monybatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "app.monybatch")
@EnableBatchProcessing(modular = true)
@EnableScheduling
@EnableConfigurationProperties
public class MonyBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonyBatchApplication.class, args);
    }

}
