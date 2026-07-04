package app.monybatch.mony.batch.dart.performance;

import app.monybatch.mony.batch.dart.cache.CorpCodeRegistry;
import app.monybatch.mony.batch.dart.classifier.DisclosureTypeClassifier;
import app.monybatch.mony.batch.dart.consumer.DartDisclosureConsumer;
import app.monybatch.mony.batch.dart.dto.CorpMappingDto;
import app.monybatch.mony.batch.dart.dto.DartRssDto;
import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.batch.dart.handler.PerformanceDisclosureHandler;
import app.monybatch.mony.batch.dart.processor.DartRssPublishProcessor;
import app.monybatch.mony.batch.dart.writer.DartRssRedisWriter;
import app.monybatch.mony.common.core.utils.OpenAPIUtil;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import app.monybatch.mony.domian.dart.processor.DisclosureProcessorFactory;
import app.monybatch.mony.domian.earning.dto.EarningsReleaseDto;
import app.monybatch.mony.domian.earning.repository.EarningsReleaseRepository;
import app.monybatch.mony.infra.llm.OllamaModelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 삼성전자 / 연결재무제표기준영업(잠정)실적(공정공시) / rcept_no=20260430800097
 *
 * RSS 감지 → 분류(BUSINESS_PERFORMANCE) → Consumer 라우팅 → 핸들러 파이프라인
 * (document.xml 다운로드 → 표 파싱 → Ollama 요약)까지 외부 의존성을 목으로 대체해 검증.
 */
@ExtendWith(MockitoExtension.class)
class PerformanceDisclosureFlowTest {

    private static final String RCEPT_NO   = "20260430800097";
    private static final String CORP_CODE  = "00126380";
    private static final String STOCK_CODE = "005930";
    private static final String CORP_NM    = "삼성전자";
    private static final String REPORT_NM  = "연결재무제표기준영업(잠정)실적(공정공시)";

    // ── Step 1: 감지(분류) ───────────────────────────────────────────────
    @Mock        CorpCodeRegistry registry;
    @Spy         DisclosureTypeClassifier classifier;
    @InjectMocks DartRssPublishProcessor publishProcessor;

    private CorpMappingDto samsungDto;

    @BeforeEach
    void setUp() {
        samsungDto = new CorpMappingDto(CORP_CODE, CORP_NM, null, STOCK_CODE);
    }

    @Test
    @DisplayName("Step1 - 연결재무제표기준영업(잠정)실적 → BUSINESS_PERFORMANCE 로 분류")
    void step1_실적공시_분류() {
        assertThat(classifier.classify(REPORT_NM))
                .isEqualTo(DisclosureType.BUSINESS_PERFORMANCE);
    }

    @Test
    @DisplayName("Step1 - RSS 수신 → 실적 분류 → DartRssQueueDto(BUSINESS_PERFORMANCE) 생성")
    void step1_실적_큐DTO_생성() throws Exception {
        when(registry.getCorpInfo(CORP_NM)).thenReturn(samsungDto);

        DartRssDto dto = new DartRssDto();
        dto.setCorpNm(CORP_NM);
        dto.setReportNm(REPORT_NM);
        dto.setRceptNo(RCEPT_NO);

        DartRssQueueDto result = publishProcessor.process(dto);

        assertThat(result).isNotNull();
        assertThat(result.getRceptNo()).isEqualTo(RCEPT_NO);
        assertThat(result.getCorpCode()).isEqualTo(CORP_CODE);
        assertThat(result.getStockCode()).isEqualTo(STOCK_CODE);
        assertThat(result.getCorpNm()).isEqualTo(CORP_NM);
        assertThat(result.getType()).isEqualTo(DisclosureType.BUSINESS_PERFORMANCE);
    }

    // ── Step 2: Consumer 라우팅 ──────────────────────────────────────────
    @Test
    @DisplayName("Step2 - Consumer: 실적 공시는 엔티티 경로 대신 PerformanceDisclosureHandler 로 위임")
    void step2_consumer_라우팅() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ListOperations<String, String> listOps = mock(ListOperations.class);
        when(redis.opsForList()).thenReturn(listOps);

        ObjectMapper mapper = new ObjectMapper();
        DartRssQueueDto item = new DartRssQueueDto(
                RCEPT_NO, CORP_CODE, STOCK_CODE, CORP_NM, DisclosureType.BUSINESS_PERFORMANCE);
        // 첫 pop = 실적 공시, 두 번째 pop = null(큐 비었음 → 종료)
        when(listOps.leftPop(DartRssRedisWriter.QUEUE_KEY))
                .thenReturn(mapper.writeValueAsString(item), (String) null);

        PerformanceDisclosureHandler handler = mock(PerformanceDisclosureHandler.class);
        DisclosureProcessorFactory factory = mock(DisclosureProcessorFactory.class);
        EntityManagerFactory emf = mock(EntityManagerFactory.class);

        DartDisclosureConsumer consumer =
                new DartDisclosureConsumer(redis, mapper, emf, factory, handler);

        consumer.consume();

        ArgumentCaptor<DartRssQueueDto> captor = ArgumentCaptor.forClass(DartRssQueueDto.class);
        verify(handler).handle(captor.capture());
        assertThat(captor.getValue().getRceptNo()).isEqualTo(RCEPT_NO);
        assertThat(captor.getValue().getType()).isEqualTo(DisclosureType.BUSINESS_PERFORMANCE);

        // 실적 공시는 일반 공시 엔티티 경로(processorFactory)를 타지 않아야 함
        verifyNoInteractions(factory);
    }

    // ── Step 3: 핸들러 파이프라인 ────────────────────────────────────────
    @Test
    @DisplayName("Step3 - 핸들러: document.xml 다운로드 → 표 파싱 → Ollama 요약 호출")
    void step3_핸들러_파이프라인() {
        OllamaModelClient ollama = mock(OllamaModelClient.class);
        EarningsReleaseRepository repository = mock(EarningsReleaseRepository.class);
        PerformanceDisclosureHandler handler = new PerformanceDisclosureHandler(ollama,repository);

        DartRssQueueDto item = new DartRssQueueDto(
                RCEPT_NO, CORP_CODE, STOCK_CODE, CORP_NM, DisclosureType.BUSINESS_PERFORMANCE);

        String expectedSummary = "005930|삼성전자|매출·영업이익 동반 증가|POSITIVE|전년 대비 외형·수익성 모두 개선";

        try (MockedStatic<OpenAPIUtil> mocked = mockStatic(OpenAPIUtil.class)) {
            // 1) document.xml → 압축해제 후 보고서 XML 을 모사한 샘플 반환
            mocked.when(() -> OpenAPIUtil.requestDartDocument(RCEPT_NO))
                    .thenReturn(sampleReportXml());
            // 3) LLM 요약은 고정값 반환
            when(ollama.summarizePerformanceDisclosure(eq(CORP_NM), eq(STOCK_CODE), new EarningsReleaseDto(),"","","",""))
                    .thenReturn(expectedSummary);

            handler.handle(item);

            // document.xml 이 rcept_no 로 호출됐는지
            mocked.verify(() -> OpenAPIUtil.requestDartDocument(RCEPT_NO));

            // 2) 파싱된 JSON 이 요약 메서드로 전달됐는지 캡처 검증
            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(ollama).summarizePerformanceDisclosure(eq(CORP_NM), eq(STOCK_CODE), new EarningsReleaseDto(),"","","","");

            String parsedJson = jsonCaptor.getValue();
            assertThat(parsedJson).contains("매출액"); // 표 대분류
            assertThat(parsedJson).contains("1000");  // 당기 실적값
            assertThat(parsedJson).contains("900");   // 전년동기 실적값
        }
    }

    @Test
    @DisplayName("Step3 - 문서 다운로드 실패(null) → 요약 호출하지 않고 종료")
    void step3_문서없음_스킵() {
        OllamaModelClient ollama = mock(OllamaModelClient.class);
        EarningsReleaseRepository repository = mock(EarningsReleaseRepository.class);
        PerformanceDisclosureHandler handler = new PerformanceDisclosureHandler(ollama,repository);

        DartRssQueueDto item = new DartRssQueueDto(
                RCEPT_NO, CORP_CODE, STOCK_CODE, CORP_NM, DisclosureType.BUSINESS_PERFORMANCE);

        try (MockedStatic<OpenAPIUtil> mocked = mockStatic(OpenAPIUtil.class)) {
            mocked.when(() -> OpenAPIUtil.requestDartDocument(RCEPT_NO)).thenReturn(null);

            handler.handle(item);

            verifyNoInteractions(ollama);
        }
    }

    /**
     * DART 뷰어 렌더링 HTML 구조(table[id*=RepeatTable] + span.xforms_input)를 모사한 샘플 보고서.
     * DartHtmlToJsonParser.parseXmlFile 의 추출 규칙(rowspan 대분류, xforms_input 값)을 그대로 탄다.
     */
    private String sampleReportXml() {
        return """
            <html><body>
            <table id="XFormD1_Form0_RepeatTable0">
              <tr>
                <td rowspan="2">매출액</td>
                <td>당기실적</td>
                <td><span class="xforms_input">1000</span></td>
              </tr>
              <tr>
                <td>전년동기실적</td>
                <td><span class="xforms_input">900</span></td>
              </tr>
            </table>
            </body></html>
            """;
    }
}
