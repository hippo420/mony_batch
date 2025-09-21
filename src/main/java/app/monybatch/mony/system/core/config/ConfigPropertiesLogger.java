package app.monybatch.mony.system.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigPropertiesLogger {
    @Autowired
    private Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void logConfigProperties() {
        log.info("🔧 [Spring Config Properties]");

        // 출력하고 싶은 config 속성 키 목록
        String[] keys = {
                "spring.datasource-batch.jdbc-url"
                , "spring.datasource-batch.username"
                , "spring.datasource-batch.password"
                , "spring.datasource-batch.driver-class-name"
                , "spring.datasource-batch.hikari.maximum-pool-size"
                , "spring.datasource-meta.jdbc-url"
                , "spring.datasource-meta.username"
                , "spring.datasource-meta.password"
                , "spring.datasource-meta.driver-class-name"
                , "spring.datasource-meta.hikari.maximum-pool-size"
        };

        for (String key : keys) {
            String value = environment.getProperty(key);
            log.info("📌 {} = {}", key, value);
        }
    }
}
