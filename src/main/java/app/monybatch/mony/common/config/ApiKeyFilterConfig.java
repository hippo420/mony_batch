package app.monybatch.mony.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ApiKeyAuthFilter를 /api/batch/** 경로에만 등록.
 * @Component로 선언하지 않는 이유: Spring Boot는 Filter 빈을 기본적으로 전체 URL("/*")에 등록하므로,
 * FilterRegistrationBean으로 명시적 urlPatterns를 지정해 배치 관리 API 이외 컨트롤러에 영향이 없게 한다.
 */
@Configuration
public class ApiKeyFilterConfig {

    // X-API-KEY 인증 임시 비활성화 — 재활성화 시 아래 주석 해제
//    @Bean
//    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyFilterRegistration(
//            @Value("${monitoring.api-key:}") String apiKey) {
//        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>(new ApiKeyAuthFilter(apiKey));
//        registration.addUrlPatterns("/api/batch/*");
//        registration.setOrder(1);
//        return registration;
//    }
}
