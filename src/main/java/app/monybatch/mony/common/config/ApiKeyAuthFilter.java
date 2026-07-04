package app.monybatch.mony.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 배치 관리 API(/api/batch/**) 전용 최소 인증. Spring Security 없이 단일 키 헤더(X-API-KEY)만 검증한다.
 * monitoring.api-key가 비어있으면(운영자 소수인 local 등) 통과시키되 기동 로그로 경고한다 — 이 경우
 * ApiKeyFilterConfig에서 /api/batch/**로 범위를 좁혀뒀으므로 다른 API에는 영향 없다.
 */
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String apiKey;

    public ApiKeyAuthFilter(String apiKey) {
        this.apiKey = apiKey;
        if (!StringUtils.hasText(apiKey)) {
            log.warn("monitoring.api-key 가 설정되지 않았습니다 — 배치 관리 API(/api/batch/**)가 인증 없이 열려 있습니다");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!StringUtils.hasText(apiKey) || apiKey.equals(request.getHeader("X-API-KEY"))) {
            chain.doFilter(request, response);
            return;
        }
        log.warn("배치 관리 API 인증 실패 - path={}, remoteAddr={}", request.getRequestURI(), request.getRemoteAddr());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"API 키가 올바르지 않습니다\"}");
    }
}
