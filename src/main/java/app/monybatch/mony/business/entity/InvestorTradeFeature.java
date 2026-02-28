package app.monybatch.mony.business.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 투자자별 수급 Feature 엔티티
 * - 메타테이블 컬럼명 기준 정규화
 * - LLM / ML 학습용 파생 컬럼 포함
 */
@Entity
@Table(name = "INVESTOR_TRADE_FEATURE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(InvestorTradeFeatureId.class)
public class InvestorTradeFeature {

    /* ===============================
       PK / 시계열 키
     =============================== */

    /** BAS_DD : 기준일자 */
    @Id
    @Column(name = "BAS_DD", nullable = false)
    private String basDd;

    /** ISU_CD : 종목코드 */
    @Id
    @Column(name = "ISU_CD", length = 12, nullable = false)
    private String isuCd;

    /* ===============================
       가격 정보
     =============================== */

    /** TDD_OPNPRC : 시가 */
    @Column(name = "TDD_OPNPRC")
    private int tddOpnprc;

    /** TDD_CLSPRC : 종가 */
    @Column(name = "TDD_CLSPRC")
    private int tddClsprc;

    /** TDD_HGPRC : 고가 */
    @Column(name = "TDD_HGPRC")
    private int tddHgprc;

    /** TDD_LWPRC : 저가 */
    @Column(name = "TDD_LWPRC")
    private int tddLwprc;

    /** FLUC_RT : 등락률 */
    @Column(name = "FLUC_RT")
    private BigDecimal flucRt;

    /* ===============================
       거래 정보
     =============================== */

    /** ACC_TRDVOL : 거래량 */
    @Column(name = "ACC_TRDVOL")
    private long accTrdvol;

    /** ACC_TRDVAL : 거래대금 */
    @Column(name = "ACC_TRDVAL")
    private long accTrdval;

    /* ===============================
       순매수 수급 (통합)
     =============================== */

    /** NET_BUY_FRGN_QTY : 외국인 순매수 */
    @Column(name = "NET_BUY_FRGN_QTY")
    private long netBuyFrgnQty;

    /** NET_BUY_INST_QTY : 기관 순매수 */
    @Column(name = "NET_BUY_INST_QTY")
    private long netBuyInstQty;

    /** NET_BUY_PRSN_QTY : 개인 순매수 */
    @Column(name = "NET_BUY_PRSN_QTY")
    private long netBuyPrsnQty;

    /** NET_BUY_TOT_QTY : 전체 순매수 */
    @Column(name = "NET_BUY_TOT_QTY")
    private long netBuyTotQty;

    /* ===============================
       외국인 세부 (중요)
     =============================== */

    /** FRGN_REG_RATIO : 외국인 등록 비중 */
    @Column(name = "FRGN_REG_RATIO")
    private BigDecimal frgnRegRatio;

    /** FRGN_NET_VAL : 외국인 순매수 대금 */
    @Column(name = "FRGN_NET_VAL")
    private long frgnNetVal;

    /* ===============================
       수급 비율
     =============================== */

    /** FRGN_BUY_RATIO : 외국인 수급 비율 */
    @Column(name = "FRGN_BUY_RATIO")
    private BigDecimal frgnBuyRatio;

    /** INST_BUY_RATIO : 기관 수급 비율 */
    @Column(name = "INST_BUY_RATIO")
    private BigDecimal instBuyRatio;

    /* ===============================
       시그널 (LLM 핵심)
     =============================== */

    /** FRGN_SIGNAL : 외국인 수급 시그널 (-5~+5) */
    @Column(name = "FRGN_SIGNAL")
    private int frgnSignal;

    /** INST_SIGNAL : 기관 수급 시그널 (-5~+5) */
    @Column(name = "INST_SIGNAL")
    private int instSignal;

    /** TOT_SIGNAL : 종합 수급 시그널 */
    @Column(name = "TOT_SIGNAL")
    private int totSignal;

    /* ===============================
       시장 해석
     =============================== */

    /** DOMINANT_ACTOR : 수급 주도 주체 (FOREIGN / INSTITUTION / PERSONAL) */
    @Column(name = "DOMINANT_ACTOR", length = 20)
    private String dominantActor;

    /** MARKET_REGIME : 시장 국면 (RISK_ON / RISK_OFF / NEUTRAL) */
    @Column(name = "MARKET_REGIME", length = 20)
    private String marketRegime;
}