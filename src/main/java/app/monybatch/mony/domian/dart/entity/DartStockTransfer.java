package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_stock_transfer",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_st", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_st_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_st_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartStockTransfer extends DartDisclosureBase {

    @Column(name = "partner_corp_name", length = 200)
    private String partnerCorpName;             // 발행회사(양도 대상) 명

    @Column(name = "transfer_share_cnt")
    private Long transferShareCnt;              // 양도주식수(주)

    @Column(name = "transfer_amount", precision = 20, scale = 0)
    private BigDecimal transferAmount;          // 양도금액(원)

    @Column(name = "total_asset", precision = 20, scale = 0)
    private BigDecimal totalAsset;             // 총자산(원)

    @Column(name = "total_asset_ratio", precision = 6, scale = 2)
    private BigDecimal totalAssetRatio;        // 총자산대비(%)

    @Column(name = "post_trf_share_cnt")
    private Long postTrfShareCnt;              // 양도 후 소유주식수(주)

    @Column(name = "post_trf_equity_ratio", precision = 6, scale = 2)
    private BigDecimal postTrfEquityRatio;     // 양도 후 지분비율(%)

    @Column(name = "purpose", length = 200)
    private String purpose;                    // 양도목적

    @Column(name = "planned_dt", length = 8)
    private String plannedDt;                  // 양도예정일자
}
