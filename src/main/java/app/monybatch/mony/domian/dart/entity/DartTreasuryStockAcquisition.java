package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_treasury_stock_acquisition",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_tsa", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_tsa_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_tsa_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartTreasuryStockAcquisition extends DartDisclosureBase {

    @Column(name = "total_acq_share_cnt")
    private Long totalAcqShareCnt;              // 총취득예정주식수 = 보통+기타

    @Column(name = "total_acq_amount", precision = 20, scale = 0)
    private BigDecimal totalAcqAmount;          // 총취득예정금액(원) = 보통+기타

    @Column(name = "existing_treasury_ratio", precision = 6, scale = 2)
    private BigDecimal existingTreasuryRatio;   // 취득 전 자기주식 보유비율(%)

    @Column(name = "acquire_purpose", length = 200)
    private String acquirePurpose;              // 취득목적

    @Column(name = "acquire_method", length = 100)
    private String acquireMethod;              // 취득방법

    @Column(name = "acquire_start_dt", length = 8)
    private String acquireStartDt;             // 취득예상기간(시작일)

    @Column(name = "acquire_end_dt", length = 8)
    private String acquireEndDt;               // 취득예상기간(종료일)

    @Column(name = "decision_dt", length = 8)
    private String decisionDt;                 // 취득결정일
}
