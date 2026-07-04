package app.monybatch.mony.infra.llm;

import app.monybatch.mony.domian.earning.dto.EarningsReleaseDto;
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
    private String PROMT_COND="중요: 반드시 한국어로 답변하세요. 중국어는 사용하지 마세요. Answer in Korean only!!!\n\n";

    public OllamaModelClient(WebClient.Builder builder,
                             @Value("${apikey.ollama.base-url}") String url,
                             @Value("${apikey.ollama.chat.options.model}") String model) {


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
                    "temperature", 0.2       // 0.0일수록 결정론적(정확), 높을수록 창의적
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

    // 1. LLM이 반환해야 할 결과물의 구조(JSON Schema)를 Map으로 정의합니다.
    // 예: { "topic": "문자열", "sentiment": "문자열", "summary": "문자열" }
    //            Map<String, Object> jsonSchema = Map.of(
    //                    "type", "object",
    //                    "properties", Map.of(
    //                            "topic", Map.of("type", "string"),
    //                            "sentiment", Map.of("type", "string"),
    //                            "summary", Map.of("type", "string")
    //                    ),
    //                    "required", List.of("topic", "sentiment", "summary")
    //            );
    public String generateJSON(String prompt, Map<String, Object> jsonSchema) {
        return executeWithTimer("Generate API", () -> {
            Map<String, Object> options = Map.of(
                    "temperature", 0.2       // 0.0일수록 결정론적(정확), 높을수록 창의적
            );

            Map<String, Object> body = Map.of(
                    "model", model,
                    "prompt", PROMT_COND + prompt,
                    "stream", false,
                    "options", options,
                    "format", jsonSchema  // 이 부분이 핵심입니다!
            );

            Map<String, Object> response = postRequest("/api/generate", body);

            // 이제 이 response 안에는 백틱(```json)이나 쓸데없는 설명 없이
            // 100% 순수한 JSON 문자열만 담겨 나오므로 바로 파싱하셔도 안전합니다.
            return Objects.requireNonNull(response).get("response").toString();
        });
    }

    public String extractKeyWord(List<String> news, int cnt) {
        StringBuilder finalResult = new StringBuilder();
        int totalSize = news.size();
        int batchSize = 10; // 5개씩 분할

        for (int i = 0; i < totalSize; i += batchSize)
        {
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
                            역할: 당신은 금융 뉴스 기반 투자 분석 시스템이다.
                    
                            입력:
                            - 뉴스 리스트 (각 뉴스는 title + content 포함)
                    
                            목표:
                            각 뉴스마다 아래를 추출한다:
                            1. 투자 키워드 (SECTOR, THEME, MACRO, COMPANY)
                            2. 투자의견 (POSITIVE / NEGATIVE / NEUTRAL)
                            3. 근거 (한 줄 요약)
                    
                            투자 키워드는 아래 중 하나로 제한한다:
                    
                                    [MACRO]
                                    금리, 환율, 유가, 인플레이션 등
                    
                                    [SECTOR]
                                    반도체(메모리, 파운드리), 2차전지, 자동차 등
                    
                                    [THEME]
                                    AI, 전기차, 로봇 등
                    
                                    [COMPANY]
                                    특정 기업명
                    
                                    규칙:
                                    1. 가장 구체적인 수준으로 선택
                                    2. 하나만 선택
                                    3. "경제", "시장" 같은 추상어 금지
                                    4. 가능하면 세부 영역 포함 (예: "반도체-메모리")
                                    5. MACRO, SECTOR, THEME, COMPANY를 직접 사용하는 것은 금지
                            
                            판단 기준:
                                    [1] 거시경제
                                    - 금리 상승 → 금융 POSITIVE / 성장 NEGATIVE
                                    - 금리 인하 → 성장 POSITIVE
                                    - 환율 상승 → 수출 POSITIVE / 내수 NEGATIVE
                                    - 유가 상승 → 에너지 POSITIVE / 운송 NEGATIVE
                    
                                    [2] 산업
                                    - 수요 증가 / 투자 확대 → POSITIVE
                                    - 규제 / 정책 제한 → NEGATIVE
                                    - 정부 지원 → POSITIVE
                    
                                    [3] 기업
                                    - 실적 증가 / 가이던스 상향 → POSITIVE
                                    - 실적 감소 / 가이던스 하향 → NEGATIVE
                                    - 리스크 이벤트(소송, 리콜) → NEGATIVE
                    
                                    [4] 시장 심리
                                    - 긍정 단어 → POSITIVE
                                    - 부정 단어 → NEGATIVE
                                    - 혼조 → NEUTRAL
                    
                                    [5] 이벤트 강도 (최우선)
                                    - 확정된 사실 → 판단 반영
                                    - 예상/전망 → NEUTRAL 우선
                    
                                    판단 우선순위:
                                    이벤트 강도 > 기업 > 산업 > 거시 > 심리
                    
                            출력 형식(JSON):
                            [
                              {
                                "type":"MACRO or SECTOR or THEME or COMPANY",
                                "keyword": "",
                                "sentiment": "POSITIVE or NEGATIVE or NEUTRAL",
                                "reason": ""
                              },
                              {
                                "type":"MACRO or SECTOR or THEME or COMPANY",
                                "keyword": "",
                                "sentiment": "POSITIVE or NEGATIVE or NEUTRAL",
                                "reason": ""
                              }
                            ]
                    
                            규칙:
                            1. 뉴스 개수와 동일한 개수 출력
                            2. 입력 순서 유지
                            3. 키워드는 반드시 명사형 (예: "반도체", "금리", "AI")
                            4. 추측 금지 (근거 없는 의견 금지)
                            5. 영어로 기사가 제공되는 경우, 한국어로 번역해 답변할 것
                            
                            기사내용
                            %s
            """, newsBuilder);



            // 4. API 호출
            String res = generate(prompt);
            res = res.substring(res.indexOf("["));
            log.info("결과물 :{}", res);
            // 5. 결과 정제 및 병합
            if (res != null && !res.trim().isEmpty()) {

                return res;
            }
        }
        return finalResult.toString();
    }

    /**
     * 영업(잠정)실적 공시 보고서(표 → JSON)를 분석/요약한다.
     *
     * DartHtmlToJsonParser.parseXmlFile 로 추출한 실적 표 JSON 을 입력받아
     * 핵심 실적 + 투자 감성을 요약한다.
     *
     * @param corpNm     기업명
     * @param stockCode  종목코드
     * @param data 실적 표를 추출한 JSON 문자열
     * @param revenueYoy 실적 표를 추출한 JSON 문자열
     * @param revenueQoq 실적 표를 추출한 JSON 문자열
     * @param profitYoy 실적 표를 추출한 JSON 문자열
     * @param profitQoq 실적 표를 추출한 JSON 문자열
     * @return "종목코드|기업명|요약|감성|감성근거" 형태의 요약 문자열
     */
    public String summarizePerformanceDisclosure(String corpNm, String stockCode, EarningsReleaseDto data ,String revenueYoy, String revenueQoq,String profitYoy, String profitQoq) {
        // String.format 대신 치환자({{변수명}})를 적용한 템플릿 사용
        String promptTemplate = """
            당신은 기업의 실적 공시 자료를 분석하는 전문 금융 AI입니다.
            제공된 공시 텍스트를 분석하여 다음 요구사항을 엄격하게 준수하여 응답하세요.
            
            
            [요구사항]
             1. 데이터 추출: 매출액, 영업이익, 당기순이익을 추출하세요. 데이터가 없으면 null로 표기하세요.
             2. 당기순이익이 없는 경우에는 잠정실적임, 실적데이터 기준으로만 분석할 것
             3. 투자 감성 분석: 실적의 투자 관점 감성을 'POSITIVE'(투자긍정), 'NEUTRAL'(투자중립), 'NEGATIVE'(투자위험) 중 하나로 반드시 분류하세요.
             4. 금액은 반드시 원 단위로 계산할 것, raw데이터의 단위를 환산해서 계산할 것. 
             5. 요약: 전체 실적에 대한 핵심 요약을 데이터의 기반해서 작성할 것, 의견이 아님. 당기순이익없는 경우 잠정실적임을 감안할것
             6. 출력 형식: 어떠한 부가 설명이나 인사말 없이 오직 아래의 JSON 형식으로만 결과를 반환하세요.

             [출력 JSON 구조]
             {
               "sentiment": "POSITIVE | NEUTRAL | NEGATIVE",
               "summary": "1분기 실적은 52조이며, 영업이익은 30조, 영업익률은 60%를 상회"
             }
            
            종목코드: {{STOCK_CODE}}
            기업명: {{CORP_NAME}}
            -실적데이터
             매출액: {{REVENUE}}
             YoY : {{REVENUE_YOY}}%
             QoQ : {{REVENUE_QOQ}}%
            
             영업이익: {{PROFIT}}
             YoY : {{PROFIT_YOY}}%
             QoQ : {{PROFIT_QOQ}}%
            
             당기순이익: {{INCOME}}
             
            {{PARSED_JSON}}
            
            
            """;

        // 연쇄적인 replace() 호출을 통해 값을 주입합니다.
        String prompt = promptTemplate
                .replace("{{STOCK_CODE}}", stockCode)
                .replace("{{CORP_NAME}}", corpNm)
                .replace("{{REVENUE}}", data.getRevenue() != null ? data.getRevenue().toString() : "없음")
                .replace("{{REVENUE_YOY}}", revenueYoy)
                .replace("{{REVENUE_QOQ}}", revenueQoq)
                .replace("{{PROFIT}}", data.getOperatingProfit() != null ? data.getOperatingProfit().toString() : "없음")
                .replace("{{INCOME}}", data.getNetIncome()  != null ? data.getNetIncome().toString() : "없음")
                .replace("{{PROFIT_YOY}}", profitYoy)
                .replace("{{PROFIT_QOQ}}", profitQoq);


        log.info("실적 공시 요약 요청: corpNm={}, stockCode={}", corpNm, stockCode);
        String result = generate(prompt);
        log.info("실적 공시 요약 결과: {}", result);

        return result;
    }

    /**
     * 영업(잠정)실적 공시 보고서(표 → JSON)를 분석/요약한다.
     *
     * DartHtmlToJsonParser.parseXmlFile 로 추출한 실적 표 JSON 을 입력받아
     * 핵심 실적 + 투자 감성을 요약한다.
     *
     * @param corpNm     기업명
     * @param stockCode  종목코드
     * @param parsedJson 실적 표를 추출한 JSON 문자열
     * @return "종목코드|기업명|요약|감성|감성근거" 형태의 요약 문자열
     */
    public String getEarningRelease(String corpNm, String stockCode, String parsedJson) {
        // String.format 대신 치환자({{변수명}})를 적용한 템플릿 사용
        String promptTemplate = """
            당신은 기업의 실적 공시 자료를 분석하는 전문 금융 AI입니다.
            제공된 공시 텍스트를 분석하여 다음 요구사항을 엄격하게 준수하여 응답하세요.
            
            [요구사항]
             1. 데이터 추출: 매출액, 영업이익, 당기순이익을 추출하세요.
             2. 금액은 반드시 원 단위로 계산할 것, raw데이터의 단위를 환산해서 계산할 것.
             3. 금액부분이 숫자가 아니면 NULL 처리할 것.
             4. 출력 형식: 어떠한 부가 설명이나 인사말 없이 오직 아래의 JSON 형식으로만 결과를 반환하세요.
  
             [출력 JSON 구조]
             {          
               "isuSrtCd": "STOCK_CODE",
               "reportYear": "연도 (예: 2026)".
               "reportQuarter": "Q1 (예: Q1, Q2, Q3, Q4, YEAR",
               "revenue":"매출액 (단위 원,precision = 19, scale = 4)",
               "operatingProfit":"영업이익 (단위 원,precision = 19, scale = 4)",
               "netIncome":"당기순이익 (단위 원,precision = 19, scale = 4)",
             }
            
            종목코드: {{STOCK_CODE}}
            기업명: {{CORP_NAME}}
            실적데이터(JSON):
            {{PARSED_JSON}}
            
            
            """;

        // 연쇄적인 replace() 호출을 통해 값을 주입합니다.
        String prompt = promptTemplate
                .replace("{{STOCK_CODE}}", stockCode)
                .replace("{{CORP_NAME}}", corpNm)
                .replace("{{PARSED_JSON}}", parsedJson);

        log.info("실적 공시 요약 요청: corpNm={}, stockCode={}", corpNm, stockCode);
        String result = generate(prompt);
        log.info("실적 공시 요약 결과: {}", result);

        return result;
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
            //log.info("\n[{}] 완료 - 소요시간: {}초\n------------------------------", taskName, duration);

            // 방법 2: 굳이 소수점 2자리까지 표현하고 싶다면 String.format을 별도로 사용
            // String message = String.format("\n[%s] 완료 - 소요시간: %.2f초", taskName, duration);
            // log.info(message);
        }
    }
}
