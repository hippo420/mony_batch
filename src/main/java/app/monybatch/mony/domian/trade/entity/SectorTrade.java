package app.monybatch.mony.domian.trade.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sector_trade_trend")
@IdClass(SectorTradeId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class SectorTrade {

    @Id
    @Column(name = "stck_bsop_date", length = 8)
    private String stckBsopDate; // 주식 영업 일자 (PK 1)

    @Id
    @Column(name = "industry_code", length = 50)
    private String industryCode; // 섹터 코드 (PK 2)

    // --- 섹터 전체 지표 ---
    @Column(name = "total_vol")
    private Long totalVol; // 섹터 총 누적 거래량

    @Column(name = "total_tr_pbmn")
    private Long totalTrPbmn; // 섹터 총 누적 거래 대금

    // --- 3대 주요 주체 순매수 지표 ---
    @Column(name = "frgn_ntby_qty")
    private Long frgnNtbyQty; // 외국인 순매수 수량

    @Column(name = "frgn_ntby_tr_pbmn")
    private Long frgnNtbyTrPbmn; // 외국인 순매수 거래 대금

    @Column(name = "orgn_ntby_qty")
    private Long orgnNtbyQty; // 기관계 순매수 수량

    @Column(name = "orgn_ntby_tr_pbmn")
    private Long orgnNtbyTrPbmn; // 기관계 순매수 거래 대금

    @Column(name = "prsn_ntby_qty")
    private Long prsnNtbyQty; // 개인 순매수 수량

    @Column(name = "prsn_ntby_tr_pbmn")
    private Long prsnNtbyTrPbmn; // 개인 순매수 거래 대금

    // --- 유의미한 세부 기관 지표 (선택 사항) ---
    @Column(name = "fund_ntby_tr_pbmn")
    private Long fundNtbyTrPbmn; // 연기금 등 순매수 거래 대금 (시장 방어/주도 역할)

    @Column(name = "pe_fund_ntby_tr_pbmn")
    private Long peFundNtbyTrPbmn; // 사모펀드 순매수 거래 대금 (단기 모멘텀 파악)

    @Builder
    public SectorTrade(String stckBsopDate, String industryCode, Long totalVol, Long totalTrPbmn,
                            Long frgnNtbyQty, Long frgnNtbyTrPbmn, Long orgnNtbyQty, Long orgnNtbyTrPbmn,
                            Long prsnNtbyQty, Long prsnNtbyTrPbmn, Long fundNtbyTrPbmn, Long peFundNtbyTrPbmn) {
        this.stckBsopDate = stckBsopDate;
        this.industryCode = industryCode;
        this.totalVol = totalVol;
        this.totalTrPbmn = totalTrPbmn;
        this.frgnNtbyQty = frgnNtbyQty;
        this.frgnNtbyTrPbmn = frgnNtbyTrPbmn;
        this.orgnNtbyQty = orgnNtbyQty;
        this.orgnNtbyTrPbmn = orgnNtbyTrPbmn;
        this.prsnNtbyQty = prsnNtbyQty;
        this.prsnNtbyTrPbmn = prsnNtbyTrPbmn;
        this.fundNtbyTrPbmn = fundNtbyTrPbmn;
        this.peFundNtbyTrPbmn = peFundNtbyTrPbmn;
    }
}
