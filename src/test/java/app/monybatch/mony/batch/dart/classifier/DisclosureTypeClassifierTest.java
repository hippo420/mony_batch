package app.monybatch.mony.batch.dart.classifier;

import app.monybatch.mony.domian.dart.entity.DisclosureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DisclosureTypeClassifierTest {

    private DisclosureTypeClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new DisclosureTypeClassifier();
    }

    @Test
    @DisplayName("현금ㆍ현물배당결정 - 대상 공시 아님 → UNKNOWN")
    void 배당결정_UNKNOWN() {
        DisclosureType result = classifier.classify("현금ㆍ현물배당결정");
        assertThat(result).isEqualTo(DisclosureType.UNKNOWN);
    }

    @ParameterizedTest(name = "[{index}] {0} → {1}")
    @DisplayName("정상 공시명 분류")
    @CsvSource({
        "유상증자결정,                                    PAID_CAPITAL_INCREASE",
        "무상증자결정,                                    FREE_CAPITAL_INCREASE",
        "감자결정,                                        CAPITAL_REDUCTION",
        "자기주식취득결정,                                TREASURY_ACQUISITION",
        "자기주식처분결정,                                TREASURY_DISPOSAL",
        "타법인주식및출자증권양수결정,                    STOCK_ACQUISITION",
        "타법인주식및출자증권양도결정,                    STOCK_TRANSFER",
        "유형자산양수결정,                                TANGIBLE_ASSET_ACQUISITION",
        "합병결정,                                        MERGER",
        "분할결정,                                        SPIN_OFF",
        "부도발생,                                        DEFAULT",
        "영업정지,                                        BUSINESS_SUSPENSION",
        "회생절차개시신청,                                REHABILITATION",
        "해산사유발생,                                    DISSOLUTION",
        "소송등의제기,                                    LAWSUIT"
    })
    void 공시유형_분류(String reportNm, DisclosureType expected) {
        assertThat(classifier.classify(reportNm.trim())).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] {0} → {1}")
    @DisplayName("브라켓 접두어 + 주요사항보고서 패턴 정규화")
    @CsvSource({
        "[기재정정]유상증자결정,                              PAID_CAPITAL_INCREASE",
        "[첨부추가]자기주식취득결정,                          TREASURY_ACQUISITION",
        "주요사항보고서(자기주식취득결정),                    TREASURY_ACQUISITION",
        "[기재정정]주요사항보고서(합병결정),                  MERGER",
        "[첨부정정]주요사항보고서(유형자산양수결정),          TANGIBLE_ASSET_ACQUISITION"
    })
    void 정규화_후_분류(String reportNm, DisclosureType expected) {
        assertThat(classifier.classify(reportNm.trim())).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] {0} → UNKNOWN")
    @DisplayName("대상 아닌 공시 → UNKNOWN")
    @CsvSource({
        "현금ㆍ현물배당결정",
        "전환사채권발행결정",
        "신주인수권부사채권발행결정",
        "임원ㆍ주요주주특정증권등소유상황보고서"
    })
    void 대상아닌공시_UNKNOWN(String reportNm) {
        assertThat(classifier.classify(reportNm)).isEqualTo(DisclosureType.UNKNOWN);
    }
}
