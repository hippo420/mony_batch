package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dart_spin_off",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_so", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_so_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_so_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartSpinOff extends DartDisclosureBase {

    @Column(name = "spin_off_method", length = 50)
    private String spinOffMethod;           // 분할방법 (인적분할/물적분할)

    @Column(name = "spin_off_ratio", length = 100)
    private String spinOffRatio;            // 분할비율

    @Column(name = "surviving_corp_name", length = 200)
    private String survivingCorpName;       // 분할 후 존속회사명

    @Column(name = "surviving_listing_yn", length = 1)
    private String survivingListingYn;      // 존속회사 상장유지 여부 (Y/N)

    @Column(name = "new_corp_name", length = 200)
    private String newCorpName;             // 분할설립회사명

    @Column(name = "new_corp_relisting_yn", length = 1)
    private String newCorpRelistingYn;      // 설립회사 재상장신청 여부 (Y/N)

    @Column(name = "spin_off_dt", length = 8)
    private String spinOffDt;              // 분할기일

    @Column(name = "shareholder_meeting_dt", length = 8)
    private String shareholderMeetingDt;   // 주주총회 예정일
}
