package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_capital_reduction",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_cr", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_cr_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_cr_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartCapitalReduction extends DartDisclosureBase {

    @Column(name = "reduction_ratio_ostk", precision = 6, scale = 2)
    private BigDecimal reductionRatioOstk;  // 감자비율(보통주식, %)

    @Column(name = "reduction_ratio_estk", precision = 6, scale = 2)
    private BigDecimal reductionRatioEstk;  // 감자비율(기타주식, %)

    @Column(name = "capital_before", precision = 20, scale = 0)
    private BigDecimal capitalBefore;       // 감자전 자본금(원)

    @Column(name = "capital_after", precision = 20, scale = 0)
    private BigDecimal capitalAfter;        // 감자후 자본금(원)

    @Column(name = "reduction_method", length = 100)
    private String reductionMethod;         // 감자방법 (유상/무상)

    @Column(name = "reduction_reason", length = 500)
    private String reductionReason;         // 감자사유 (500자 이내)

    @Column(name = "std_dt", length = 8)
    private String stdDt;                   // 감자기준일

    @Column(name = "shareholder_meeting_dt", length = 8)
    private String shareholderMeetingDt;    // 주주총회 예정일
}
