package app.monybatch.mony.domian.trade.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Partition
 * --------------------------------------------------------------------------------------------
 * PARTITION BY RANGE COLUMNS(stck_bsop_date) (
 *     PARTITION p202604 VALUES LESS THAN ('20260501'),
 *     PARTITION p202605 VALUES LESS THAN ('20260601'),
 *     PARTITION p202606 VALUES LESS THAN ('20260701'),
 *     PARTITION p_max VALUES LESS THAN (MAXVALUE) -- 미래 데이터를 위한 기본 파티션 (선택 사항)
 * );
 * --------------------------------------------------------------------------------------------
 */
@Entity
@Table(name = "pile_trade")
@IdClass(TradeId.class) // 복합키 식별자 클래스 지정
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Trade {

    @Id
    private String stckBsopDate; // 주식 영업 일자 (PK 1)

    @Id
    private String isuSrtCd;     // 종목 단축 코드 (PK 2)

    private String stckClpr; // 주식 종가
    private String prdyVrss; // 전일 대비
    private String prdyVrssSign; // 전일 대비 부호
    private String prdyCtrt; // 전일 대비율
    private String acmlVol; // 누적 거래량 (단위 : 주)
    private String acmlTrPbmn; // 누적 거래 대금 (단위 : 백만원)
    private String stckOprc; // 주식 시가2
    private String stckHgpr; // 주식 최고가
    private String stckLwpr; // 주식 최저가

    // --- 순매수 수량 및 대금 ---
    private String frgnNtbyQty; // 외국인 순매수 수량
    private String frgnRegNtbyQty; // 외국인 등록 순매수 수량
    private String frgnNregNtbyQty; // 외국인 비등록 순매수 수량
    private String prsnNtbyQty; // 개인 순매수 수량
    private String orgnNtbyQty; // 기관계 순매수 수량
    private String scrtNtbyQty; // 증권 순매수 수량
    private String ivtrNtbyQty; // 투자신탁 순매수 수량
    private String peFundNtbyVol; // 사모 펀드 순매수 거래량
    private String bankNtbyQty; // 은행 순매수 수량
    private String insuNtbyQty; // 보험 순매수 수량
    private String mrbnNtbyQty; // 종금 순매수 수량
    private String fundNtbyQty; // 기금 순매수 수량
    private String etcNtbyQty; // 기타 순매수 수량
    private String etcCorpNtbyVol; // 기타 법인 순매수 거래량
    private String etcOrgtNtbyVol; // 기타 단체 순매수 거래량

    private String frgnRegNtbyPbmn; // 외국인 등록 순매수 대금
    private String frgnNtbyTrPbmn; // 외국인 순매수 거래 대금
    private String frgnNregNtbyPbmn; // 외국인 비등록 순매수 대금
    private String prsnNtbyTrPbmn; // 개인 순매수 거래 대금
    private String orgnNtbyTrPbmn; // 기관계 순매수 거래 대금
    private String scrtNtbyTrPbmn; // 증권 순매수 거래 대금
    private String peFundNtbyTrPbmn; // 사모 펀드 순매수 거래 대금
    private String ivtrNtbyTrPbmn; // 투자신탁 순매수 거래 대금
    private String bankNtbyTrPbmn; // 은행 순매수 거래 대금
    private String insuNtbyTrPbmn; // 보험 순매수 거래 대금
    private String mrbnNtbyTrPbmn; // 종금 순매수 거래 대금
    private String fundNtbyTrPbmn; // 기금 순매수 거래 대금
    private String etcNtbyTrPbmn; // 기타 순매수 거래 대금
    private String etcCorpNtbyTrPbmn; // 기타 법인 순매수 거래 대금
    private String etcOrgtNtbyTrPbmn; // 기타 단체 순매수 거래 대금

    // --- 외국인 세부 매도/매수 ---
    private String frgnSelnVol; // 외국인 매도 거래량
    private String frgnShnuVol; // 외국인 매수2 거래량
    private String frgnSelnTrPbmn; // 외국인 매도 거래 대금
    private String frgnShnuTrPbmn; // 외국인 매수2 거래 대금
    private String frgnRegAskpQty; // 외국인 등록 매도 수량
    private String frgnRegBidpQty; // 외국인 등록 매수 수량
    private String frgnRegAskpPbmn; // 외국인 등록 매도 대금
    private String frgnRegBidpPbmn; // 외국인 등록 매수 대금
    private String frgnNregAskpQty; // 외국인 비등록 매도 수량
    private String frgnNregBidpQty; // 외국인 비등록 매수 수량
    private String frgnNregAskpPbmn; // 외국인 비등록 매도 대금
    private String frgnNregBidpPbmn; // 외국인 비등록 매수 대금

    // --- 개인/기관/기타 주체별 매도/매수 ---
    private String prsnSelnVol; // 개인 매도 거래량
    private String prsnShnuVol; // 개인 매수2 거래량
    private String prsnSelnTrPbmn; // 개인 매도 거래 대금
    private String prsnShnuTrPbmn; // 개인 매수2 거래 대금

    private String orgnSelnVol; // 기관계 매도 거래량
    private String orgnShnuVol; // 기관계 매수2 거래량
    private String orgnSelnTrPbmn; // 기관계 매도 거래 대금
    private String orgnShnuTrPbmn; // 기관계 매수2 거래 대금

    private String scrtSelnVol; // 증권 매도 거래량
    private String scrtShnuVol; // 증권 매수2 거래량
    private String scrtSelnTrPbmn; // 증권 매도 거래 대금
    private String scrtShnuTrPbmn; // 증권 매수2 거래 대금

    private String ivtrSelnVol; // 투자신탁 매도 거래량
    private String ivtrShnuVol; // 투자신탁 매수2 거래량
    private String ivtrSelnTrPbmn; // 투자신탁 매도 거래 대금
    private String ivtrShnuTrPbmn; // 투자신탁 매수2 거래 대금

    private String peFundSelnVol; // 사모 펀드 매도 거래량
    private String peFundShnuVol; // 사모 펀드 매수2 거래량
    private String peFundSelnTrPbmn; // 사모 펀드 매도 거래 대금
    private String peFundShnuTrPbmn; // 사모 펀드 매수2 거래 대금

    private String bankSelnVol; // 은행 매도 거래량
    private String bankShnuVol; // 은행 매수2 거래량
    private String bankSelnTrPbmn; // 은행 매도 거래 대금
    private String bankShnuTrPbmn; // 은행 매수2 거래 대금

    private String insuSelnVol; // 보험 매도 거래량
    private String insuShnuVol; // 보험 매수2 거래량
    private String insuSelnTrPbmn; // 보험 매도 거래 대금
    private String insuShnuTrPbmn; // 보험 매수2 거래 대금

    private String mrbnSelnVol; // 종금 매도 거래량
    private String mrbnShnuVol; // 종금 매수2 거래량
    private String mrbnSelnTrPbmn; // 종금 매도 거래 대금
    private String mrbnShnuTrPbmn; // 종금 매수2 거래 대금

    private String fundSelnVol; // 기금 매도 거래량
    private String fundShnuVol; // 기금 매수2 거래량
    private String fundSelnTrPbmn; // 기금 매도 거래 대금
    private String fundShnuTrPbmn; // 기금 매수2 거래 대금

    private String etcSelnVol; // 기타 매도 거래량
    private String etcShnuVol; // 기타 매수2 거래량
    private String etcSelnTrPbmn; // 기타 매도 거래 대금
    private String etcShnuTrPbmn; // 기타 매수2 거래 대금

    private String etcOrgtSelnVol; // 기타 단체 매도 거래량
    private String etcOrgtShnuVol; // 기타 단체 매수2 거래량
    private String etcOrgtSelnTrPbmn; // 기타 단체 매도 거래 대금
    private String etcOrgtShnuTrPbmn; // 기타 단체 매수2 거래 대금

    private String etcCorpSelnVol; // 기타 법인 매도 거래량
    private String etcCorpShnuVol; // 기타 법인 매수2 거래량
    private String etcCorpSelnTrPbmn; // 기타 법인 매도 거래 대금
    private String etcCorpShnuTrPbmn; // 기타 법인 매수2 거래 대금

    private String boldYn; // BOLD 여부
}
