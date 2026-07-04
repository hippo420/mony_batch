package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_stock_acquisition",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_sa", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_sa_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_sa_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartStockAcquisition extends DartDisclosureBase {

    @Column(name = "partner_corp_name", length = 200)
    private String partnerCorpName;             // 발행회사(양수 대상) 명

    @Column(name = "acquisition_share_cnt")
    private Long acquisitionShareCnt;           // 양수주식수(주)

    @Column(name = "acquisition_amount", precision = 20, scale = 0)
    private BigDecimal acquisitionAmount;       // 양수금액(원)

    @Column(name = "total_asset", precision = 20, scale = 0)
    private BigDecimal totalAsset;              // 총자산(원)

    @Column(name = "total_asset_ratio", precision = 6, scale = 2)
    private BigDecimal totalAssetRatio;         // 총자산대비(%)

    @Column(name = "post_acq_share_cnt")
    private Long postAcqShareCnt;              // 양수 후 소유주식수(주)

    @Column(name = "post_acq_equity_ratio", precision = 6, scale = 2)
    private BigDecimal postAcqEquityRatio;     // 양수 후 지분비율(%)

    @Column(name = "purpose", length = 200)
    private String purpose;                    // 양수목적

    @Column(name = "planned_dt", length = 8)
    private String plannedDt;                  // 양수예정일자

    @Column(name = "backdoor_listing_yn", length = 1)
    private String backdoorListingYn;          // 우회상장 해당여부 (Y/N)
}
