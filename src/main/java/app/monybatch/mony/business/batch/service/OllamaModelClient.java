package app.monybatch.mony.business.batch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class OllamaModelClient  {
    private final WebClient webClient;
    private final String model;
    private String PROMT_COND="중요: 반드시 한국어로 답변하세요. 중국어는 사용하지 마세요.\n\n";
    public OllamaModelClient(WebClient.Builder builder,
                             @Value("${apikey.ollama.base-url}") String url,
                             @Value("${apikey.ollama.chat.options.model}") String model) {

        log.info("INIT url: {}",url);
        this.model=model;
        this.webClient = builder
                .baseUrl(url)
                .build();
    }

    /**
     * 1. /api/generate 방식 (단일 프롬프트)
     */
    public String generate(String prompt) {
        return executeWithTimer("Generate API", () -> {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "prompt", PROMT_COND+prompt,
                    "stream", false
            );

            Map<String, Object> response = postRequest("/api/generate", body);
            return Objects.requireNonNull(response).get("response").toString();
        });
    }

    /**
     * 2. /api/chat 방식 (메시지 리스트)
     */
    public String chat(String prompt) {
        return executeWithTimer("Chat API", () -> {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", PROMT_COND+prompt)),
                    "stream", false
            );

            Map<String, Object> response = postRequest("/api/chat", body);
            // Chat API는 응답 구조가 message -> content 계층임
            Map<String, Object> message = (Map<String, Object>) response.get("message");
            return message.get("content").toString();
        });
    }

    /**
     * 공통 요청 메서드
     */
    private Map<String, Object> postRequest(String uri, Map<String, Object> body) {
        return webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    /**
     * 공통 실행 시간 측정 로직
     */
    private <T> T executeWithTimer(String taskName, java.util.function.Supplier<T> task) {
        long start = System.currentTimeMillis();
        try {
            return task.get();
        } finally {
            long end = System.currentTimeMillis();
            double duration = (end - start) / 1000.0;

            // 방법 1: SLF4J의 기본 중괄호 방식 (가장 추천)
            log.info("\n[{}] 완료 - 소요시간: {}초\n------------------------------", taskName, duration);

            // 방법 2: 굳이 소수점 2자리까지 표현하고 싶다면 String.format을 별도로 사용
            // String message = String.format("\n[%s] 완료 - 소요시간: %.2f초", taskName, duration);
            // log.info(message);
        }
    }
}
