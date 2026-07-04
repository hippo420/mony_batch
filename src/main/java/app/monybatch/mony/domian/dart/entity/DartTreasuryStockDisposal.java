package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_treasury_stock_disposal",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_tsd", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_tsd_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_tsd_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartTreasuryStockDisposal extends DartDisclosureBase {

    @Column(name = "total_disposal_share_cnt")
    private Long totalDisposalShareCnt;         // 총처분예정주식수 = 보통+기타

    @Column(name = "total_disposal_amount", precision = 20, scale = 0)
    private BigDecimal totalDisposalAmount;     // 총처분예정금액(원) = 보통+기타

    @Column(name = "disposal_purpose", length = 200)
    private String disposalPurpose;             // 처분목적

    @Column(name = "disposal_method", length = 100)
    private String disposalMethod;             // 처분방법 (시장매도/시간외/장외/기타 중 최대)

    @Column(name = "disposal_start_dt", length = 8)
    private String disposalStartDt;            // 처분예정기간(시작일)

    @Column(name = "disposal_end_dt", length = 8)
    private String disposalEndDt;              // 처분예정기간(종료일)

    @Column(name = "decision_dt", length = 8)
    private String decisionDt;                 // 처분결정일
}
