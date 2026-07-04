package app.monybatch.mony.domian.earning.entity;

import app.monybatch.mony.domian.earning.dto.ReportQuarter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter @Setter
@Entity
@Table(name = "earnings_release", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"isu_srd_cd", "report_year", "report_quarter"})
})
public class EarningsRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isu_srd_cd", nullable = false, length = 10)
    private String isuSrtCd; // 종목코드 (예: 005930)

    // 연도는 정수형으로 분리
    @Column(name = "report_year", nullable = false)
    private Integer reportYear;

    // Enum을 문자열 형태로 DB에 저장
    @Enumerated(EnumType.STRING)
    @Column(name = "report_quarter", nullable = false, length = 10)
    private ReportQuarter reportQuarter;

    // 금액 데이터는 반드시 BigDecimal 사용
    @Column(precision = 19, scale = 4)
    private BigDecimal revenue; // 매출액

    @Column(name = "operating_profit", precision = 19, scale = 4)
    private BigDecimal operatingProfit; // 영업이익

    @Column(name = "net_income", precision = 19, scale = 4)
    private BigDecimal netIncome; // 당기순이익

    // 퍼센트 등 비율 데이터
    @Column(name = "yoy_growth")
    private Double yoyGrowth; // 전년 동기 대비 성장률

    @Column(name = "qoq_growth")
    private Double qoqGrowth; // 전분기 대비 증감률

    // 요약
    @Column(name = "summary")
    private String summary; // 요약

}
