package app.monybatch.mony.batch.dart.processor;

import app.monybatch.mony.batch.dart.cache.CorpCodeRegistry;
import app.monybatch.mony.batch.dart.classifier.DisclosureTypeClassifier;
import app.monybatch.mony.batch.dart.dto.CorpMappingDto;
import app.monybatch.mony.batch.dart.dto.DartRssDto;
import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DartRssPublishProcessorTest {

    @Mock
    private CorpCodeRegistry registry;

    @Spy
    private DisclosureTypeClassifier classifier;

    @InjectMocks
    private DartRssPublishProcessor processor;

    private static final String SAMSUNG_CORP_CODE = "00126380";
    private static final String SAMSUNG_STOCK_CODE = "005930";
    private static final String RCEPT_NO = "20260430000001";

    private CorpMappingDto samsungDto;

    @BeforeEach
    void setUp() {
        samsungDto = new CorpMappingDto(SAMSUNG_CORP_CODE, "삼성전자", null, SAMSUNG_STOCK_CODE);
    }

    @Test
    @DisplayName("삼성전자 / 현금ㆍ현물배당결정 → 대상 아닌 공시 → null 반환(스킵)")
    void 배당결정_스킵() throws Exception {
        DartRssDto dto = buildDto("삼성전자", "현금ㆍ현물배당결정");
        when(registry.getCorpInfo("삼성전자")).thenReturn(samsungDto);

        DartRssQueueDto result = processor.process(dto);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("삼성전자 / 유상증자결정 → 큐 DTO 정상 반환")
    void 유상증자_정상처리() throws Exception {
        DartRssDto dto = buildDto("삼성전자", "유상증자결정");
        when(registry.getCorpInfo("삼성전자")).thenReturn(samsungDto);

        DartRssQueueDto result = processor.process(dto);

        assertThat(result).isNotNull();
        assertThat(result.getRceptNo()).isEqualTo(RCEPT_NO);
        assertThat(result.getCorpCode()).isEqualTo(SAMSUNG_CORP_CODE);
        assertThat(result.getType()).isEqualTo(DisclosureType.PAID_CAPITAL_INCREASE);
    }

    @Test
    @DisplayName("[기재정정]주요사항보고서(자기주식취득결정) → TREASURY_ACQUISITION 분류 후 큐 적재")
    void 브라켓접두어_정규화후_분류() throws Exception {
        DartRssDto dto = buildDto("삼성전자", "[기재정정]주요사항보고서(자기주식취득결정)");
        when(registry.getCorpInfo("삼성전자")).thenReturn(samsungDto);

        DartRssQueueDto result = processor.process(dto);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(DisclosureType.TREASURY_ACQUISITION);
    }

    @Test
    @DisplayName("Reader에서 이미 corpCode 세팅된 경우 → registry 재조회 없이 처리")
    void corpCode_이미세팅() throws Exception {
        DartRssDto dto = buildDto("삼성전자", "합병결정");
        dto.setCorpCode(SAMSUNG_CORP_CODE); // Reader에서 이미 세팅

        DartRssQueueDto result = processor.process(dto);

        assertThat(result).isNotNull();
        assertThat(result.getCorpCode()).isEqualTo(SAMSUNG_CORP_CODE);
        assertThat(result.getType()).isEqualTo(DisclosureType.MERGER);
        // registry 미호출 확인
    }

    @Test
    @DisplayName("캐시 미매핑 회사명 → null 반환(스킵)")
    void 미매핑회사_스킵() throws Exception {
        DartRssDto dto = buildDto("존재하지않는회사", "유상증자결정");
        when(registry.getCorpInfo("존재하지않는회사")).thenReturn(null);

        DartRssQueueDto result = processor.process(dto);

        assertThat(result).isNull();
    }

    // ── helper ──────────────────────────────────────────────────────────

    private DartRssDto buildDto(String corpNm, String reportNm) {
        DartRssDto dto = new DartRssDto();
        dto.setCorpNm(corpNm);
        dto.setReportNm(reportNm);
        dto.setRceptNo(RCEPT_NO);
        return dto;
    }
}
