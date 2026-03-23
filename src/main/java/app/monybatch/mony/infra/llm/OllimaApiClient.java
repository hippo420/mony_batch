package app.monybatch.mony.infra.llm;

import app.monybatch.mony.domian.news.entity.NewsDto;
import app.monybatch.mony.domian.report.entity.Keyword;
import app.monybatch.mony.domian.report.entity.ReportDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OllimaApiClient {

    private Client client;

    private String model;

    public OllimaApiClient(@Value("${apikey.gemini.api.key}") String apiKey) {
        this.client = null; // 초기화는 아래 init()에서 수행하도록 변경

    }


    @PostConstruct
    public void init() {

        log.info("GeminiApiClient 초기화 완료. (API Key 설정 확인)");
    }

    /**
     * Gemini에게 뉴스 요약 + 감성 분석을 요청하는 메서드
     *
     * @return "요약|감성" 형태의 문자열
     */
    public String requestSummaryAndSentiment(List<NewsDto> newsList) {

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
                String requestPromt = sb.toString().replaceAll("<b>","").replaceAll("</b>","");

        log.info("prompt = {}", sb.toString());
        try {
            // ★ Gemini API 호출 (1.28.0)
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            requestPromt,
                            null
                    );

            // ★ 응답 텍스트
            String result = response.text()!= null ? response.text().trim() : "";

            log.info("Gemini 응답: {}", result);

            return result;

        } catch (Exception e) {
            log.error("Gemini API 호출 오류: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public String requestSummaryAndSentimentOne(NewsDto news) {

        // ★ 프롬프트 생성
        String prompt = String.format("""
                아래 뉴스 기사를 분석해줘.
                1) 3줄 이내 요약
                """);
        StringBuilder sb = new StringBuilder();
        sb.append(prompt);

            sb.append(news.getTitle());
            sb.append("|");
            sb.append(news.getDescription());
            sb.append("|");
            sb.append(news.getPubDate());

        log.info("prompt = {}", sb.toString());
        try {
            // ★ Gemini API 호출 (1.28.0)
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            sb.toString(),
                            null
                    );

            // ★ 응답 텍스트
            String result = response.text()!= null ? response.text().trim() : "";

            log.info("Gemini 응답: {}", result);

            return result;

        } catch (Exception e) {
            log.error("Gemini API 호출 오류: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public String extractKeyWord(List<NewsDto> news, int cnt) {

        // ★ 프롬프트 생성
        String prompt = String.format("""
                너는 금융서비스를 제공하는 AI에이전트야.
                아래 뉴스 기사에서 투자자게 필요한 중요한 키워드를 추출해줘.
                사용자는 한국사람이야 반드시 한국어로 대답해.
                1) 여러 개의 뉴스를 파이프(|)로 구분해서 입력함.
                2) 중요한 %s개의 키워드를 추출할 것.
                2.1) 각 뉴스기사의 키워드는 파이프(|)로 구분해 출력할 것.
                2.2) 각 키워드는 콤마(,)로 구분할 것.
                """,cnt);

        StringBuilder sb = new StringBuilder();
        for (NewsDto item: news)
        {
            sb.append(item.getDescription());
            sb.append("|");
        }
        sb.deleteCharAt(sb.length()-1);


        log.info("prompt = {}", sb.toString());
        try {
            // ★ Gemini API 호출 (1.28.0)
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            sb.toString(),
                            null
                    );

            // ★ 응답 텍스트
            String result = response.text()!= null ? response.text().trim() : "";

            log.info("Gemini 응답: {}", result);

            return result;

        } catch (Exception e) {
            log.error("Gemini API 호출 오류: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }


    public String requestReportSummary(List<ReportDto> reportList) {

        // ★ 프롬프트 생성
        String prompt = String.format("""
                아래 애널리스트 보고서를 요약해줘.
                1) 종목의 목표가, 투자의견, 키워드등을 요약할거임
                2) 요약근거로 투자의견을 매도(SELL)-> 중립(NETURAL) -> 매수(BUY)로 구분하고 없으면 NULL로 처리
                3) 종목이나 목표가가 없으면 NULL로 처리
                4) 제공하는 보고서가 여러개인 경우 ^로 구분
                5) PDF파일을 tika라이브러리로 추출했기 때문에 표를 추출한 내용을 감안해줘
                6) 핵심키워드는 보고서에서 핵심키워드 2,3개 선정
                7) 결과는 "일자(YYYYMMDD)|증권사|작성자(이름만)|종목|투자의견|목표가|핵심키워드" 형태로 반환하고 여러 개의 보고서를 요약시 ^으로 구분
                """);
        StringBuilder sb = new StringBuilder();
        sb.append(prompt);

        for(int i =0;i<reportList.size();i++) {
            sb.append(reportList.get(i).getContent());
            if(i!=reportList.size()-1) {
                sb.append("^");
            }
        }
        String requestPromt = sb.toString().replaceAll("<b>","").replaceAll("</b>","");

        log.info("prompt = {}", sb.toString());
        try {
            // ★ Gemini API 호출 (1.28.0)
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            requestPromt,
                            null
                    );

            // ★ 응답 텍스트
            String result = response.text()!= null ? response.text().trim() : "";

            log.info("Gemini 응답: {}", result);

            return result;

        } catch (Exception e) {
            log.error("Gemini API 호출 오류: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String,List<Keyword>> extractKeywords(String content)
    {
        // 1. 프롬프트 수정: JSON 형식을 표준 Map 구조인 { "key": value } 형태로 요청합니다.
        String prompt = """
        다음 리포트 내용에서 핵심 키워드 3개를 추출해줘
        1) 종목명 제외, 회사명 제외.
        2) 긍정도(0.0~1.0)를 추출해줘, 0.5가 기준값임
        3) 반드시 아래 표준 JSON 객체 형식으로만 응답해.
           응답 형식: {"종목코드": [{"keyword": "단어", "score": 0.9}, ...], "종목코드2": [...]}
        4) 입력 데이터 형식: 종목코드:리포트내용|종목코드:리포트내용...
        내용:
        """ + content;

            try {
                // ★ Gemini API 호출
                GenerateContentResponse response = client.models.generateContent(
                        "gemini-2.5-flash", // 현재 사용 가능한 최신 모델명 확인 필요
                        prompt,
                        null
                );

                String result = response.text() != null ? response.text().trim() : "";

                // 마크다운 코드 블록(```json) 제거
                if (result.contains("```")) {
                    result = result.replaceAll("(?s)```(?:json)?|```", "").trim();
                }

                log.info("Gemini 추출 결과: {}", result);

                // 2. Jackson을 사용하여 Map<String, List<Keyword>>로 변환
                ObjectMapper objectMapper = new ObjectMapper();

                // TypeReference를 사용하여 명확하게 HashMap 구조로 읽어옵니다.
                return objectMapper.readValue(result, new TypeReference<Map<String, List<Keyword>>>() {});

            } catch (Exception e) {
                log.error("Gemini API 호출 및 파싱 오류: {}", e.getMessage(), e);
                return new HashMap<>(); // 에러 발생 시 빈 맵 반환 혹은 예외 처리
            }

    }
}
