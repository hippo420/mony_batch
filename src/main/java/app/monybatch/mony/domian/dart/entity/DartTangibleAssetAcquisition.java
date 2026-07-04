package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_tangible_asset_acquisition",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_taa", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_taa_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_taa_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartTangibleAssetAcquisition extends DartDisclosureBase {

    @Column(name = "asset_type", length = 50)
    private String assetType;                   // 자산구분

    @Column(name = "asset_name", length = 200)
    private String assetName;                  // 자산명

    @Column(name = "acquisition_amount", precision = 20, scale = 0)
    private BigDecimal acquisitionAmount;      // 양수금액(원)

    @Column(name = "total_asset", precision = 20, scale = 0)
    private BigDecimal totalAsset;             // 자산총액(원)

    @Column(name = "total_asset_ratio", precision = 6, scale = 2)
    private BigDecimal totalAssetRatio;        // 자산총액대비(%)

    @Column(name = "purpose", length = 200)
    private String purpose;                    // 양수목적

    @Column(name = "impact", length = 500)
    private String impact;                     // 양수영향 (500자 이내)

    @Column(name = "planned_contract_dt", length = 8)
    private String plannedContractDt;          // 계약체결일

    @Column(name = "planned_acq_dt", length = 8)
    private String plannedAcqDt;               // 양수기준일
}
