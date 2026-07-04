package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_default",
        uniqueConstraints = @UniqueConstraint(name = "uk_dart_df", columnNames = {"rcept_no"}),
        indexes = {
                @Index(name = "idx_df_corp_code",  columnList = "corp_code"),
                @Index(name = "idx_df_stock_code", columnList = "stock_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartDefault extends DartDisclosureBase {

    @Column(name = "default_amount", precision = 20, scale = 0)
    private BigDecimal defaultAmount;       // 부도금액(원)

    @Column(name = "default_bank", length = 200)
    private String defaultBank;            // 부도발생은행

    @Column(name = "default_reason", length = 500)
    private String defaultReason;          // 부도사유 (500자 이내)

    @Column(name = "default_dt", length = 8)
    private String defaultDt;              // 최종부도(당좌거래정지)일자
}
