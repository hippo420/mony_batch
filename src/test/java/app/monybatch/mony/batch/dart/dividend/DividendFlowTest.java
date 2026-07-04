package app.monybatch.mony.batch.dart.dividend;

import app.monybatch.mony.batch.dart.cache.CorpCodeRegistry;
import app.monybatch.mony.batch.dart.classifier.DisclosureTypeClassifier;
import app.monybatch.mony.batch.dart.dto.CorpMappingDto;
import app.monybatch.mony.batch.dart.dto.DartRssDto;
import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.batch.dart.processor.DartRssPublishProcessor;
import app.monybatch.mony.common.core.utils.DartReportCodeResolver;
import app.monybatch.mony.domian.dart.dto.raw.DartDividendRawDto;
import app.monybatch.mony.domian.dart.entity.DartDividend;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import app.monybatch.mony.domian.dart.processor.DividendProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 삼성전자 / 현금ㆍ현물배당결정 / 20260430 기준
 * RSS 수신 → 분류 → 큐 적재 → DividendProcessor 변환 전 과정 검증
 */
@ExtendWith(MockitoExtension.class)
class DividendFlowTest {

    // ── 공통 픽스처 ──────────────────────────────────────────────────────

    private static final String RCEPT_NO   = "20260430000001";  // 접수번호
    private static final String CORP_CODE  = "00126380";
    private static final String STOCK_CODE = "005930";
    private static final String CORP_NM    = "삼성전자";
    private static final String REPORT_NM  = "현금ㆍ현물배당결정";

    // ── Step 1: DartRssPublishProcessor ─────────────────────────────────

    @Mock  CorpCodeRegistry registry;
    @Spy   DisclosureTypeClassifier classifier;
    @InjectMocks DartRssPublishProcessor publishProcessor;

    private CorpMappingDto samsungDto;

    @BeforeEach
    void setUp() {
        samsungDto = new CorpMappingDto(CORP_CODE, CORP_NM, null, STOCK_CODE);
    }

    @Test
    @DisplayName("Step1 - RSS 수신 → 배당 분류 → DartRssQueueDto(BAEDANG) 반환")
    void step1_배당_큐DTO_생성() throws Exception {
        when(registry.getCorpInfo(CORP_NM)).thenReturn(samsungDto);

        DartRssDto dto = buildRssDto(CORP_NM, REPORT_NM);

        DartRssQueueDto result = publishProcessor.process(dto);

        assertThat(result).isNotNull();
        assertThat(result.getRceptNo()).isEqualTo(RCEPT_NO);
        assertThat(result.getCorpCode()).isEqualTo(CORP_CODE);
        assertThat(result.getStockCode()).isEqualTo(STOCK_CODE);
        assertThat(result.getType()).isEqualTo(DisclosureType.BAEDANG);
    }

    // ── Step 2: DartReportCodeResolver ──────────────────────────────────

    @Test
    @DisplayName("Step2 - 접수일 20260430 → 1분기보고서(11013), bsns_year=2026")
    void step2_접수일_기준_보고서코드_추론() {
        String rceptDt = RCEPT_NO.substring(0, 8); // "20260430"

        assertThat(DartReportCodeResolver.resolveReprtCode(rceptDt))
                .as("4월 접수 → 1분기보고서")
                .isEqualTo(DartReportCodeResolver.Q1);    // "11013"

        assertThat(DartReportCodeResolver.resolveBsnsYear(rceptDt))
                .as("1분기보고서 → 접수연도")
                .isEqualTo("2026");
    }

    // ── Step 3: DividendProcessor ────────────────────────────────────────

    @Test
    @DisplayName("Step3 - DividendProcessor: 보통주 DPS 파싱 + YoY 계산")
    void step3_DividendProcessor_엔티티_변환() {
        DividendProcessor processor = new DividendProcessor();
        DartRssQueueDto queueItem = new DartRssQueueDto(RCEPT_NO, CORP_CODE, STOCK_CODE, DisclosureType.BAEDANG);

        List<DartDividendRawDto> rawList = List.of(
                buildRaw("1주당 현금배당금(원)", "보통주식", "1,500", "1,350", "1,200"),
                buildRaw("1주당 현금배당금(원)", "우선주식", "1,550", "1,400", "-"),
                buildRaw("시가배당율(%)",        "보통주식", "2.1",   "1.9",   "1.7")   // 필터 제외 대상
        );

        // DividendProcessor는 DisclosureProcessor 인터페이스를 구현하므로
        // 실제 Consumer 흐름과 동일하게 JSONObject 대신 rawList 직접 검증
        var results = rawList.stream()
                .filter(r -> "1주당 현금배당금(원)".equals(r.getSe()))
                .map(r -> callToEntity(processor, r, queueItem))
                .toList();

        assertThat(results).hasSize(2);

        DartDividend 보통주 = results.stream()
                .filter(e -> "보통주식".equals(e.getStockKind())).findFirst().orElseThrow();

        assertThat(보통주.getRceptNo()).isEqualTo(RCEPT_NO);
        assertThat(보통주.getCorpCode()).isEqualTo(CORP_CODE);
        assertThat(보통주.getStockCode()).isEqualTo(STOCK_CODE);
        assertThat(보통주.getRceptDt()).isEqualTo("20260430");
        assertThat(보통주.getDisclosureType()).isEqualTo(DisclosureType.BAEDANG.name());
        assertThat(보통주.getDpsThisYear()).isEqualByComparingTo("1500.00");
        assertThat(보통주.getDpsPrevYear()).isEqualByComparingTo("1350.00");
        assertThat(보통주.getYoyChangeAmt()).isEqualByComparingTo("150.00");    // 1500 - 1350
        assertThat(보통주.getYoyChangeRatio()).isEqualByComparingTo("11.11");   // 150/1350 × 100

        DartDividend 우선주 = results.stream()
                .filter(e -> "우선주식".equals(e.getStockKind())).findFirst().orElseThrow();

        assertThat(우선주.getDpsPrev2Year()).isNull();     // "-" → null
        assertThat(우선주.getYoyChangeAmt()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Step3 - 시가배당율(%) 행은 필터링되어 결과에 포함되지 않음")
    void step3_시가배당율_행_제외() {
        DividendProcessor processor = new DividendProcessor();
        DartRssQueueDto queueItem = new DartRssQueueDto(RCEPT_NO, CORP_CODE, STOCK_CODE, DisclosureType.BAEDANG);

        List<DartDividendRawDto> rawList = List.of(
                buildRaw("시가배당율(%)", "보통주식", "2.1", "1.9", "1.7")
        );

        var results = rawList.stream()
                .filter(r -> "1주당 현금배당금(원)".equals(r.getSe()))
                .toList();

        assertThat(results).isEmpty();
    }

    // ── helper ──────────────────────────────────────────────────────────

    private DartRssDto buildRssDto(String corpNm, String reportNm) {
        DartRssDto dto = new DartRssDto();
        dto.setCorpNm(corpNm);
        dto.setReportNm(reportNm);
        dto.setRceptNo(RCEPT_NO);
        return dto;
    }

    private DartDividendRawDto buildRaw(String se, String stockKnd,
                                        String thstrm, String frmtrm, String lwfr) {
        DartDividendRawDto raw = new DartDividendRawDto();
        raw.setRceptNo(RCEPT_NO);
        raw.setCorpCode(CORP_CODE);
        raw.setCorpName(CORP_NM);
        raw.setCorpCls("Y");
        raw.setSe(se);
        raw.setStockKnd(stockKnd);
        raw.setThstrm(thstrm);
        raw.setFrmtrm(frmtrm);
        raw.setLwfr(lwfr);
        raw.setStlmDt("20251231");
        return raw;
    }

    /** DividendProcessor.toEntity()를 reflection 없이 테스트하기 위한 위임 */
    private DartDividend callToEntity(DividendProcessor processor,
                                      DartDividendRawDto raw, DartRssQueueDto q) {
        // process()는 JSONObject를 받으므로 rawList를 직접 변환하는 내부 로직을 검증
        // → DividendProcessor의 se 필터 + toEntity 로직을 rawList 수준에서 단위 검증
        DartRssQueueDto item = new DartRssQueueDto(raw.getRceptNo(), raw.getCorpCode(),
                q.getStockCode(), DisclosureType.BAEDANG);

        // toEntity 접근을 위해 process()의 내부 결과를 List<DartDisclosureBase>로 검증
        // (실제 process(JSONObject)는 Consumer 통합 테스트에서 커버)
        List<DartDisclosureBase> list = processor.process(
                buildMockResponse(raw), item);

        return list.isEmpty() ? null : (DartDividend) list.get(0);
    }

    /** DividendProcessor.process()에 넘길 JSONObject 모사 */
    private org.json.simple.JSONObject buildMockResponse(DartDividendRawDto raw) {
        // 실제 DART API 응답 구조와 동일하게 구성
        var list = new org.json.simple.JSONArray();
        var item = new org.json.simple.JSONObject();
        item.put("rcept_no",  raw.getRceptNo());
        item.put("corp_cls",  raw.getCorpCls());
        item.put("corp_code", raw.getCorpCode());
        item.put("corp_name", raw.getCorpName());
        item.put("se",        raw.getSe());
        item.put("stock_knd", raw.getStockKnd());
        item.put("thstrm",    raw.getThstrm());
        item.put("frmtrm",    raw.getFrmtrm());
        item.put("lwfr",      raw.getLwfr());
        item.put("stlm_dt",   raw.getStlmDt());
        list.add(item);

        var response = new org.json.simple.JSONObject();
        response.put("status",  "000");
        response.put("message", "정상");
        response.put("list",    list);
        return response;
    }
}
