package app.monybatch.mony.domian.dart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_dividend",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dart_div", columnNames = {"rcept_no", "stock_kind"})
        },
        indexes = {
                @Index(name = "idx_div_corp_code", columnList = "corp_code"),
                @Index(name = "idx_div_stock_code", columnList = "stock_code"),
                @Index(name = "idx_div_rcept_no",  columnList = "rcept_no")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartDividend extends DartDisclosureBase {

    @Column(name = "stock_kind", length = 50, nullable = false)
    private String stockKind; // 주식 종류 (보통주식 / 우선주식)

    @Column(name = "dps_this_year", precision = 20, scale = 2)
    private BigDecimal dpsThisYear; // 당기 주당배당금(원)

    @Column(name = "dps_prev_year", precision = 20, scale = 2)
    private BigDecimal dpsPrevYear; // 전기 주당배당금(원)

    @Column(name = "dps_prev2_year", precision = 20, scale = 2)
    private BigDecimal dpsPrev2Year; // 전전기 주당배당금(원)

    @Column(name = "yoy_change_amt", precision = 20, scale = 2)
    private BigDecimal yoyChangeAmt; // 전기 대비 증감액(원) = dpsThisYear - dpsPrevYear

    @Column(name = "yoy_change_ratio", precision = 6, scale = 2)
    private BigDecimal yoyChangeRatio; // 전기 대비 증감률(%) — 전기 0이면 null

    @Column(name = "settlement_dt", length = 8)
    private String settlementDt; // 결산기준일 (yyyyMMdd)
}
