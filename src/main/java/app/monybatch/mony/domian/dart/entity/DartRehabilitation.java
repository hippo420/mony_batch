package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dart_rehabilitation",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_rh", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_rh_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_rh_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartRehabilitation extends DartDisclosureBase {

    @Column(name = "applicant", length = 200)
    private String applicant;               // 신청인(회사와의 관계)

    @Column(name = "court", length = 200)
    private String court;                  // 관할법원

    @Column(name = "request_reason", length = 500)
    private String requestReason;          // 신청사유 (500자 이내)

    @Column(name = "request_dt", length = 8)
    private String requestDt;              // 신청일자
}
