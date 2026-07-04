package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dart_lawsuit",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_ls", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_ls_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_ls_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartLawsuit extends DartDisclosureBase {

    @Column(name = "case_name", length = 300)
    private String caseName;                // 사건의 명칭

    @Column(name = "plaintiff", length = 200)
    private String plaintiff;              // 원고·신청인

    @Column(name = "claim_summary", length = 500)
    private String claimSummary;           // 청구내용 (500자 이내)

    @Column(name = "court", length = 200)
    private String court;                  // 관할법원

    @Column(name = "filing_dt", length = 8)
    private String filingDt;               // 제기일자
}
