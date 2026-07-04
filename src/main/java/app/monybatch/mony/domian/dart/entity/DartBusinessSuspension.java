package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_business_suspension",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_bs", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_bs_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_bs_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartBusinessSuspension extends DartDisclosureBase {

    @Column(name = "suspension_amount", precision = 20, scale = 0)
    private BigDecimal suspensionAmount;    // 영업정지금액(원)

    @Column(name = "revenue_ratio", precision = 6, scale = 2)
    private BigDecimal revenueRatio;        // 매출액 대비(%)

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;        // 영업정지사유 (500자 이내)

    @Column(name = "suspension_dt", length = 8)
    private String suspensionDt;            // 영업정지일자
}
