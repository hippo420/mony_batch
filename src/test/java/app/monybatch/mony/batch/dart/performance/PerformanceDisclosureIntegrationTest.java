package app.monybatch.mony.batch.dart.performance;

import app.monybatch.mony.common.core.utils.DartHtmlToJsonParser;
import app.monybatch.mony.common.core.utils.OpenAPIUtil;
import app.monybatch.mony.domian.earning.dto.EarningsReleaseDto;
import app.monybatch.mony.infra.llm.OllamaModelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 삼성전자 / 연결재무제표기준영업(잠정)실적(공정공시) / rcept_no=20260430800097
 *
 * 실제 OpenDART document.xml 다운로드 → 압축해제 → 표 파싱 → 실제 Ollama 요약까지
 * 전체 파이프라인을 라이브로 검증한다.
 *
 * 외부 의존성(DART 인증키 + 로컬 Ollama 서버)이 필요하므로 기본 비활성화.
 * 로컬에서 수동 실행 시 @Disabled 를 제거하고 실행하세요.
 */
//@Disabled("실제 DART document.xml 다운로드 + 로컬 Ollama 서버 필요 - 수동 실행 전용")
@SpringBootTest
@ActiveProfiles("local")
class PerformanceDisclosureIntegrationTest {

    @Autowired
    private OllamaModelClient ollamaModelClient;

    private static final String RCEPT_NO   = "20260430800097";
    private static final String CORP_CODE  = "00126380";
    private static final String STOCK_CODE = "005930";
    private static final String CORP_NM    = "삼성전자";

    @Test
    @DisplayName("삼성전자 실적 공시 - 문서 다운로드 → 파싱 → Ollama 요약 라이브 검증")
    void 실적공시_전체파이프라인_라이브() throws Exception {

        // ── 1. document.xml 다운로드 → ZIP 해제 → 보고서 XML ──────────────
        String reportXml = OpenAPIUtil.requestDartDocument(RCEPT_NO);
        System.out.printf("%n=== 보고서 XML 길이: %d ===%n",
                reportXml == null ? 0 : reportXml.length());
        assertThat(reportXml).as("DART 원본 문서 다운로드 결과").isNotBlank();

        // ── 2. 보고서 XML → 실적 표 JSON ─────────────────────────────────
        String parsedJson = DartHtmlToJsonParser.parseXmlFile(reportXml);
        System.out.printf("%n=== 파싱된 실적 JSON ===%n%s%n", parsedJson);
        assertThat(parsedJson).as("실적 표 추출 결과(키:값 존재)").contains(":");

        // ── 3. Ollama 분석/요약 ───────────────────────────────────────────
        String summary = ollamaModelClient.getEarningRelease(
                CORP_NM, STOCK_CODE, parsedJson);
        System.out.printf("%n=== Ollama 요약 결과 ===%n%s%n", summary);
        EarningsReleaseDto current = null;
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            current = objectMapper.readValue(summary, EarningsReleaseDto.class);
        }
        catch (Exception e)
        {
            System.out.println("JSON 변환오류 : " +e.getMessage());
        }

        String summary1 = ollamaModelClient.summarizePerformanceDisclosure(
                CORP_NM, STOCK_CODE, current,"69.16","42.67","756.10","185.11");


        System.out.printf("%n=== Ollama 요약 결과 ===%n%s%n", summary1);
        assertThat(summary).as("LLM 요약 결과").isNotBlank();
    }
}
