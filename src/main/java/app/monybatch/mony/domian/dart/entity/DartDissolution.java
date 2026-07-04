package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dart_dissolution",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_ds", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_ds_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_ds_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartDissolution extends DartDisclosureBase {

    @Column(name = "dissolution_reason", length = 500)
    private String dissolutionReason;       // 해산사유 (500자 이내)

    @Column(name = "dissolution_dt", length = 8)
    private String dissolutionDt;           // 해산사유발생일
}
