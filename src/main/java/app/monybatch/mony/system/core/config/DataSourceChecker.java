package app.monybatch.mony.system.core.config;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class DataSourceChecker {

    @Value("${spring.datasource.url:}")
    private String url;

    @PostConstruct
    public void check() {
        log.info("DataSourceChecker.check()");
        log.info("spring.datasource.url: " + url);

    }
}
