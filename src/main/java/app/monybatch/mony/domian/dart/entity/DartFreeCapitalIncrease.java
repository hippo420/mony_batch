package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_free_capital_increase",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_fci", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_fci_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_fci_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartFreeCapitalIncrease extends DartDisclosureBase {

    @Column(name = "new_share_ostk")
    private Long newShareOstk;              // 신주(보통주식, 주)

    @Column(name = "new_share_estk")
    private Long newShareEstk;              // 신주(기타주식, 주)

    @Column(name = "allot_ratio_ostk", precision = 10, scale = 4)
    private BigDecimal allotRatioOstk;      // 1주당 신주배정(보통주)

    @Column(name = "allot_ratio_estk", precision = 10, scale = 4)
    private BigDecimal allotRatioEstk;      // 1주당 신주배정(기타주)

    @Column(name = "allot_std_dt", length = 8)
    private String allotStdDt;              // 신주배정기준일

    @Column(name = "listing_dt", length = 8)
    private String listingDt;              // 신주 상장예정일

    @Column(name = "board_decision_dt", length = 8)
    private String boardDecisionDt;        // 이사회결의일
}
