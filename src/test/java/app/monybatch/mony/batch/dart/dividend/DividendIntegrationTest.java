package app.monybatch.mony.batch.dart.dividend;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.common.constant.StockConstant;
import app.monybatch.mony.common.core.utils.DartReportCodeResolver;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.common.core.utils.OpenAPIUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartDividendRawDto;
import app.monybatch.mony.domian.dart.entity.DartDividend;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import app.monybatch.mony.domian.dart.processor.DividendProcessor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class DividendIntegrationTest {

    @Autowired
    private DividendProcessor dividendProcessor;

    @Autowired
    @Qualifier("batchEntityManager")
    private EntityManagerFactory entityManagerFactory;

    private static int len(String s) { return s == null ? 0 : s.length(); }

    private static final String CORP_CODE  = "00126380";
    private static final String STOCK_CODE = "005930";
    private static final String RCEPT_DT   = "20260430";

    @Test
    @DisplayName("삼성전자 배당 - DART API 실제 조회 → DB 저장 → 결과 검증")
    void 삼성전자_배당_실적재_검증() {

        // ── 1. reprt_code / bsns_year 추론 ───────────────────────────────
        String reprtCode = DartReportCodeResolver.resolveReprtCode(RCEPT_DT);
        String bsnsYear  = DartReportCodeResolver.resolveBsnsYear(RCEPT_DT);
        System.out.printf("%n=== 조회 파라미터 ===%nbsns_year=%s, reprt_code=%s%n", bsnsYear, reprtCode);

        // ── 2. DART API 호출 ──────────────────────────────────────────────
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("corp_code",  CORP_CODE);
        params.add("bsns_year",  bsnsYear);
        params.add("reprt_code", reprtCode);

        JSONObject response = OpenAPIUtil.requestApi(
                StockConstant.DART_API_URL,
                DisclosureType.BAEDANG.getApiPath(),
                params, Map.of(), DataType.DATA_JSON);

        assertThat(response).isNotNull();
        assertThat(response.get("status")).as("DART API 정상응답").isEqualTo("000");

        // ── 3. 진단: 실제 se 값 목록 출력 ────────────────────────────────
        List<DartDividendRawDto> rawList = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartDividendRawDto.class);

        System.out.printf("%n=== API 응답 파싱 결과 (총 %d건) ===%n", rawList.size());
        rawList.forEach(r -> System.out.printf(
                "  se=[%s] | stock_knd=[%s] | thstrm=[%s] | frmtrm=[%s]%n",
                r.getSe(), r.getStockKnd(), r.getThstrm(), r.getFrmtrm()));

        // se 값 종류 출력 → DPS_SE 상수와 비교용
        System.out.println("\n=== 고유 se 값 ===");
        rawList.stream()
                .map(DartDividendRawDto::getSe)
                .distinct()
                .forEach(se -> System.out.printf("  [%s] (length=%d)%n", se, se == null ? 0 : se.length()));

        assertThat(rawList).as("API 응답에 데이터가 있어야 함").isNotEmpty();

        // ── 4. Processor 변환 ─────────────────────────────────────────────
        DartRssQueueDto queueItem = new DartRssQueueDto(
                RCEPT_DT + "000001", CORP_CODE, STOCK_CODE, DisclosureType.BAEDANG);

        List<DartDisclosureBase> entities = dividendProcessor.process(response, queueItem);

        System.out.printf("%n=== DividendProcessor 변환 결과 (%d건) ===%n", entities.size());
        entities.forEach(e -> {
            DartDividend d = (DartDividend) e;
            System.out.printf("  [%s] stockKind=%-6s | dpsThis=%s | dpsPrev=%s | yoyAmt=%s | yoyRatio=%s%%%n",
                    d.getCorpName(), d.getStockKind(),
                    d.getDpsThisYear(), d.getDpsPrevYear(),
                    d.getYoyChangeAmt(), d.getYoyChangeRatio());
        });

        assertThat(entities).as("변환된 엔티티가 1건 이상").isNotEmpty();

        // ── 5. DB 저장 전 필드값 덤프 (varchar 초과 진단) ──────────────────
        System.out.println("\n=== 저장 전 엔티티 필드 덤프 ===");
        entities.forEach(e -> {
            DartDividend d = (DartDividend) e;
            System.out.printf("  rcept_no(%d)=[%s]%n",      len(d.getRceptNo()),      d.getRceptNo());
            System.out.printf("  rcept_dt(%d)=[%s]%n",      len(d.getRceptDt()),      d.getRceptDt());
            System.out.printf("  corp_code(%d)=[%s]%n",     len(d.getCorpCode()),     d.getCorpCode());
            System.out.printf("  corp_name(%d)=[%s]%n",     len(d.getCorpName()),     d.getCorpName());
            System.out.printf("  stock_code(%d)=[%s]%n",    len(d.getStockCode()),    d.getStockCode());
            System.out.printf("  stock_kind(%d)=[%s]%n",    len(d.getStockKind()),    d.getStockKind());
            System.out.printf("  settlement_dt(%d)=[%s]%n", len(d.getSettlementDt()), d.getSettlementDt());
            System.out.printf("  disclosure_type(%d)=[%s]%n", len(d.getDisclosureType()), d.getDisclosureType());
        });

        // ── 6. DB 저장 ────────────────────────────────────────────────────
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            entities.forEach(em::merge);
            tx.commit();
            System.out.println("\n=== DB 저장 완료 ===");
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }

        // ── 7. DB 조회 검증 ───────────────────────────────────────────────
        EntityManager verifyEm = entityManagerFactory.createEntityManager();
        try {
            List<DartDividend> saved = verifyEm.createQuery(
                            "SELECT d FROM DartDividend d " +
                            "WHERE d.corpCode = :corpCode ORDER BY d.stockKind",
                            DartDividend.class)
                    .setParameter("corpCode", CORP_CODE)
                    .getResultList();

            System.out.printf("%n=== DB 저장 결과 (%d건) ===%n", saved.size());
            saved.forEach(d -> System.out.printf(
                    "  id=%-5d | %-6s | dpsThis=%8s | dpsPrev=%8s | yoyRatio=%s%%%n",
                    d.getId(), d.getStockKind(),
                    d.getDpsThisYear(), d.getDpsPrevYear(), d.getYoyChangeRatio()));

            assertThat(saved).isNotEmpty();
            assertThat(saved).allSatisfy(d -> {
                assertThat(d.getCorpCode()).isEqualTo(CORP_CODE);
                assertThat(d.getStockCode()).isEqualTo(STOCK_CODE);
                assertThat(d.getDisclosureType()).isEqualTo(DisclosureType.BAEDANG.name());
                assertThat(d.getDpsThisYear()).isNotNull();
            });
        } finally {
            verifyEm.close();
        }
    }
}
