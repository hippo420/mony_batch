package app.monybatch.mony.batch.stock.processor;

import app.monybatch.mony.domian.stock.dto.InvestorTradeRaw;
import app.monybatch.mony.domian.stock.entity.InvestorTradeFeature;

import java.math.BigDecimal;

/**
 * InvestorTradeRaw → InvestorTradeFeature 변환 로직
 * - FULL RAW 기반 수급 Feature 생성
 * - LLM / 시계열 학습용
 */
public class InvestorTradeProcessor {

    private final String basDd;
    private static final double DOMINANT_RATIO = 0.55;

    public InvestorTradeProcessor(String basDd) {
        this.basDd = basDd;
    }

    public InvestorTradeFeature processInternal(InvestorTradeRaw raw) {

        /* ===============================
           1. 가격 / 거래 정보
         =============================== */
        int tddOpnprc = toInt(raw.getStckOprc());
        int tddClsprc = toInt(raw.getStckClpr());
        int tddHgprc  = toInt(raw.getStckHgpr());
        int tddLwprc  = toInt(raw.getStckLwpr());
        double flucRt = toDouble(raw.getPrdyCtrt());

        long accTrdvol = toLong(raw.getAcmlVol());
        long accTrdval = toLong(raw.getAcmlTrPbmn()); // 단위: 백만원

        /* ===============================
           2. 순매수 수량
         =============================== */
        long frgnNetQty = toLong(raw.getFrgnNtbyQty());
        long prsnNetQty = toLong(raw.getPrsnNtbyQty());

        long instNetQty =
                toLong(raw.getOrgnNtbyQty())
                        + toLong(raw.getIvtrNtbyQty())
                        + toLong(raw.getBankNtbyQty())
                        + toLong(raw.getInsuNtbyQty())
                        + toLong(raw.getFundNtbyQty())
                        + toLong(raw.getMrbnNtbyQty());

        long totalNetQty = frgnNetQty + instNetQty + prsnNetQty;

        // 의미 없는 row 제거
        if (totalNetQty == 0) {
            return null;
        }

        /* ===============================
           3. 외국인 세부 (등록 / 비등록)
         =============================== */
        long frgnRegQty  = toLong(raw.getFrgnRegNtbyQty());
        long frgnNregQty = toLong(raw.getFrgnNregNtbyQty());

        double frgnRegRatio = ratio(frgnRegQty, frgnRegQty + frgnNregQty);

        long frgnNetVal = toLong(raw.getFrgnNtbyTrPbmn());

        /* ===============================
           4. 수급 비율
         =============================== */
        double frgnBuyRatio = ratio(frgnNetQty, totalNetQty);
        double instBuyRatio = ratio(instNetQty, totalNetQty);

        /* ===============================
           5. 수급 시그널 (-5 ~ +5)
         =============================== */
        int frgnSignal = calcSignal(frgnBuyRatio, frgnNetQty);
        int instSignal = calcSignal(instBuyRatio, instNetQty);
        int totSignal  = clamp(frgnSignal + instSignal, -5, 5);

        /* ===============================
           6. 주도 주체 판단
         =============================== */
        String dominantActor;
        if (frgnBuyRatio >= DOMINANT_RATIO) {
            dominantActor = "FOREIGN";
        } else if (instBuyRatio >= DOMINANT_RATIO) {
            dominantActor = "INSTITUTION";
        } else {
            dominantActor = "PERSONAL";
        }

        /* ===============================
           7. 시장 국면 판단
         =============================== */
        String marketRegime = decideMarketRegime(frgnSignal, flucRt);

        /* ===============================
           8. Feature 엔티티 생성
         =============================== */
        return InvestorTradeFeature.builder()
                // PK
                .basDd(raw.getStckBsopDate())
                .isuCd(raw.getIsuSrtCd())

                // 가격
                .tddOpnprc(tddOpnprc)
                .tddClsprc(tddClsprc)
                .tddHgprc(tddHgprc)
                .tddLwprc(tddLwprc)
                .flucRt(BigDecimal.valueOf(flucRt))

                // 거래
                .accTrdvol(accTrdvol)
                .accTrdval(accTrdval)

                // 순매수
                .netBuyFrgnQty(frgnNetQty)
                .netBuyInstQty(instNetQty)
                .netBuyPrsnQty(prsnNetQty)
                .netBuyTotQty(totalNetQty)

                // 외국인 세부
                .frgnRegRatio(round(frgnRegRatio))
                .frgnNetVal(frgnNetVal)

                // 비율
                .frgnBuyRatio(round(frgnBuyRatio))
                .instBuyRatio(round(instBuyRatio))

                // 시그널
                .frgnSignal(frgnSignal)
                .instSignal(instSignal)
                .totSignal(totSignal)

                // 해석
                .dominantActor(dominantActor)
                .marketRegime(marketRegime)

                .build();
    }

    /* ===============================
       Helper Methods
     =============================== */

    private long toLong(String v) {
        if (v == null || v.isBlank()) return 0L;
        return Long.parseLong(v.replace(",", ""));
    }

    private int toInt(String v) {
        if (v == null || v.isBlank()) return 0;
        return Integer.parseInt(v.replace(",", ""));
    }

    private double toDouble(String v) {
        if (v == null || v.isBlank()) return 0.0;
        return Double.parseDouble(v);
    }

    private double ratio(long part, long total) {
        if (total == 0) return 0.0;
        return (double) part / Math.abs(total);
    }

    private int calcSignal(double ratio, long qty) {
        if (qty == 0) return 0;

        if (ratio >= 0.70) return qty > 0 ? 5 : -5;
        if (ratio >= 0.55) return qty > 0 ? 4 : -4;
        if (ratio >= 0.40) return qty > 0 ? 3 : -3;
        if (ratio >= 0.25) return qty > 0 ? 2 : -2;
        return qty > 0 ? 1 : -1;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private BigDecimal round(double v) {
        return BigDecimal.valueOf(Math.round(v * 10000.0) / 10000.0);
    }

    private String decideMarketRegime(int frgnSignal, double flucRt) {
        if (frgnSignal >= 3 && flucRt > 0) return "RISK_ON";
        if (frgnSignal <= -3 && flucRt < 0) return "RISK_OFF";
        return "NEUTRAL";
    }
}