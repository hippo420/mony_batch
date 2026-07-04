package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_paid_capital_increase",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_pci", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_pci_corp_code", columnList = "corp_code"),
                @Index(name = "idx_pci_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartPaidCapitalIncrease extends DartDisclosureBase {

    @Column(name = "new_share_ostk")
    private Long newShareOstk;              // 신주(보통주식, 주)

    @Column(name = "new_share_estk")
    private Long newShareEstk;              // 신주(기타주식, 주)

    @Column(name = "dilution_ratio", precision = 6, scale = 2)
    private BigDecimal dilutionRatio;       // 희석률(%) = 신주 / (기존+신주) × 100

    @Column(name = "total_fund_amount", precision = 20, scale = 0)
    private BigDecimal totalFundAmount;     // 총 자금조달금액(원) = fdpp 합산

    @Column(name = "main_fund_purpose", length = 50)
    private String mainFundPurpose;         // 주요 자금조달목적 (금액 최대 항목)

    @Column(name = "increase_method", length = 100)
    private String increaseMethod;          // 증자방식 (공모/제3자배정/주주배정)

    @Column(name = "short_sell_applied", length = 1)
    private String shortSellApplied;        // 공매도 해당여부 (Y/N)

    @Column(name = "short_sell_start_dt", length = 8)
    private String shortSellStartDt;        // 공매도 시작일

    @Column(name = "short_sell_end_dt", length = 8)
    private String shortSellEndDt;          // 공매도 종료일
}
