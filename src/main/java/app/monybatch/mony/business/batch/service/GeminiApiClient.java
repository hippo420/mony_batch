package app.monybatch.mony.business.batch.service;

import app.monybatch.mony.business.entity.news.News;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GeminiApiClient {

    private Client client;
    private final String apiKey; // 설정 파일에서 주입받을 필드

    public GeminiApiClient(@Value("${apikey.gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = null; // 초기화는 아래 init()에서 수행하도록 변경
    }


    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("GEMINI_API_KEY 설정이 누락되었습니다.");
        }
        try {
            // Client 객체를 API Key를 사용하여 생성합니다.
            //this.client = new Client(this.apiKey);

            // 만약 빌더 패턴을 사용해야 한다면:
             this.client = Client.builder().apiKey(this.apiKey).build();

            log.info("GeminiApiClient 초기화 완료. (API Key 설정 확인)");
        } catch (Exception e) {
            log.error("GeminiClient 초기화 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("Gemini API 클라이언트 초기화에 실패했습니다.", e);
        }


        log.info("GeminiApiClient 초기화 완료. (API Key 설정 확인)");
    }

    /**
     * Gemini에게 뉴스 요약 + 감성 분석을 요청하는 메서드
     *
     * @return "요약|감성" 형태의 문자열
     */
    public String requestSummaryAndSentiment(List<News> newsList) {

        // ★ 프롬프트 생성
        String prompt = String.format("""
                아래 뉴스 기사를 분석해줘.

                1) 3줄 이내 요약
                2) 기사 감성을 POSITIVE(투자긍정) 또는 NEUTRAL(투자중립) 또는 NEGATIVE(투자위험) 로 분류
                3) 내가 제공하는 기사는 제목|내용|발행일자로 구분
                4) 제공하는 기사가 여러개인 경우 ^로 구분
                5) 핵심키워드는 기사에서 핵심키워드 2,3개 선정, 감성근거는 해당 감성으로 분류되었는지"에 대한 1문장짜리 근거를 추가
                6) 결과는 "년월일|제목요약|요약내용|감성|핵심키워드|감성근거" 형태로 반환하고 여러 개의 기사를 분석시 ^으로 구분
                
                """);
                StringBuilder sb = new StringBuilder();
                sb.append(prompt);

                for(int i =0;i<newsList.size();i++) {
                    sb.append(newsList.get(i).getTitle());
                    sb.append("|");
                    sb.append(newsList.get(i).getDescription());
                    sb.append("|");
                    sb.append(newsList.get(i).getPubDate());
                    if(i!=newsList.size()-1) {
                        sb.append("^");
                    }
                }


        try {
            // ★ Gemini API 호출 (1.28.0)
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.0-flash",
                            sb.toString(),
                            null
                    );

            // ★ 응답 텍스트
            String result = response.text().trim();

            log.info("Gemini 응답: {}", result);

            return result;

        } catch (Exception e) {
            log.error("Gemini API 호출 오류: {}", e.getMessage(), e);
            return "API_ERROR|NEUTRAL";
        }
    }
}
