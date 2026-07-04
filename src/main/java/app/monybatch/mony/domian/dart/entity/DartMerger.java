package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_merger",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_mg", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_mg_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_mg_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartMerger extends DartDisclosureBase {

    @Column(name = "merger_method", length = 50)
    private String mergerMethod;                    // 합병방법 (흡수합병/신설합병)

    @Column(name = "merger_ratio", length = 100)
    private String mergerRatio;                     // 합병비율

    @Column(name = "merger_purpose", length = 500)
    private String mergerPurpose;                   // 합병목적 (500자 이내)

    @Column(name = "partner_corp_name", length = 200)
    private String partnerCorpName;                 // 합병상대회사명

    @Column(name = "partner_total_asset", precision = 20, scale = 0)
    private BigDecimal partnerTotalAsset;           // 합병상대 자산총계(원)

    @Column(name = "partner_net_income", precision = 20, scale = 0)
    private BigDecimal partnerNetIncome;            // 합병상대 당기순이익(원)

    @Column(name = "backdoor_listing_yn", length = 1)
    private String backdoorListingYn;               // 우회상장 해당여부 (Y/N)

    @Column(name = "merger_dt", length = 8)
    private String mergerDt;                        // 합병기일

    @Column(name = "shareholder_meeting_dt", length = 8)
    private String shareholderMeetingDt;            // 주주총회 예정일

    @Column(name = "new_share_listing_dt", length = 8)
    private String newShareListingDt;               // 신주 상장예정일
}
