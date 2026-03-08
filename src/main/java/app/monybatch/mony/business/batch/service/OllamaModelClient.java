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
            Map<String, Object> options = Map.of(
                    "temperature", 0.0       // 0.0일수록 결정론적(정확), 높을수록 창의적
            );
            Map<String, Object> body = Map.of(
                    "model", model,
                    "prompt", PROMT_COND+prompt,
                    "stream", false,
                    "options", options
            );

            Map<String, Object> response = postRequest("/api/generate", body);
            return Objects.requireNonNull(response).get("response").toString();
        });
    }

    public String extractKeyWord(List<String> news, int cnt) {
        StringBuilder finalResult = new StringBuilder();
        int totalSize = news.size();
        int batchSize = 5; // 5개씩 분할

        for (int i = 0; i < totalSize; i += batchSize) {
            // 1. 5개씩 안전하게 자르기
            int end = Math.min(i + batchSize, totalSize);
            List<String> subList = news.subList(i, end);
            int currentBatchSize = subList.size();

            // 2. 현재 배치를 위한 뉴스 문자열 생성
            StringBuilder newsBuilder = new StringBuilder();
            for (int j = 0; j < currentBatchSize; j++) {
                newsBuilder.append(String.format("기사%d: %s\n", j + 1, subList.get(j)));
            }

            // 3. 분할 처리를 위한 프롬프트 구성
            String prompt = String.format("""
                    당신은 전문 경제 분석가입니다.
                    
                    아래 뉴스 %d개를 분석하여 각 뉴스마다
                    1개의 투자 키워드와 1개의 투자의견을 추출하세요.
                
                    [투자의견 기준]
                    POSITIVE : 투자에 긍정적
                    NEGATIVE : 투자에 부정적
                    NEUTRAL : 판단 불가 또는 중립
                
                    [출력 규칙]
                    1. 뉴스 하나당 반드시 결과 1개를 생성한다.
                    2. 출력 개수는 반드시 %d개여야 한다.
                    3. 결과는 반드시 다음 형식을 따른다.
                    4. 출력순서는 반드시 입력 순서에 따른다.
                    키워드,투자의견|키워드,투자의견|키워드,투자의견
                
                    4. 키워드는 반드시 명사 한 단어만 사용한다.
                    5. 설명, 인사말, 기사 번호, 추가 텍스트는 절대 출력하지 않는다.
                    6. 형식이 틀리면 다시 생성한다.
                    7. 키워드 위치와 투자의견 위치를 절대 바꾸지 않는다.
                    8. 반드시 아래 출력 예시와 동일한 형식으로만 출력한다.
                
                    [출력 예시]
                    반도체,POSITIVE|금리,NEGATIVE|배당,POSITIVE
                
                    [대상 뉴스]
                    %s
                
                    [최종 출력]
            """, currentBatchSize, currentBatchSize, newsBuilder.toString());



            // 4. API 호출
            String res = generate(prompt);
            //log.info("결과물 :{}", res);
            // 5. 결과 정제 및 병합
            if (res != null && !res.trim().isEmpty()) {
                String cleanedRes = res.trim()
                        .replace("\n", "|")
                        .replaceAll("\\|{2,}", "|")
                        .replaceAll("\\s+", "");

                if (finalResult.length() > 0 && !cleanedRes.isEmpty()) {
                    finalResult.append("|");
                }
                finalResult.append(cleanedRes);
            }
        }

        //log.info("Final Combined Result = {}", finalResult.toString());
        return finalResult.toString();
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
