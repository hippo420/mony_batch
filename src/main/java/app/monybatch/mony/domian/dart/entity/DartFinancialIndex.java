package app.monybatch.mony.domian.dart.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_financial_index",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dart_fi",
                        columnNames = {"corp_code", "bsns_year", "reprt_code", "idx_code"}
                )
        },
        indexes = {
                @Index(name = "idx_fi_corp_year", columnList = "corp_code,bsns_year"),
                @Index(name = "idx_fi_cl_code", columnList = "idx_cl_code")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartFinancialIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "reprt_code", length = 5, nullable = false)
    @JsonProperty("reprt_code")
    private String reprtCode; // 보고서코드 (11011:사업, 11012:반기, 11013:1분기, 11014:3분기)

    @Column(name = "bsns_year", length = 4, nullable = false)
    @JsonProperty("bsns_year")
    private String bsnsYear; // 사업연도

    @Column(name = "corp_code", length = 8, nullable = false)
    @JsonProperty("corp_code")
    private String corpCode; // 고유번호

    @Column(name = "stock_code", length = 6)
    @JsonProperty("stock_code")
    private String stockCode; // 종목코드

    @Column(name = "stlm_dt", length = 8)
    @JsonProperty("stlm_dt")
    private String stlmDt; // 결산기준일

    @Column(name = "idx_cl_code", length = 10)
    @JsonProperty("idx_cl_code")
    private String idxClCode; // 지표분류코드

    @Column(name = "idx_cl_nm", length = 50)
    @JsonProperty("idx_cl_nm")
    private String idxClNm; // 지표분류명

    @Column(name = "idx_code", length = 20, nullable = false)
    @JsonProperty("idx_code")
    private String idxCode; // 지표코드

    @Column(name = "idx_nm", length = 100)
    @JsonProperty("idx_nm")
    private String idxNm; // 지표명

    @Column(name = "idx_val", precision = 20, scale = 2)
    @JsonProperty("idx_val")
    private BigDecimal idxVal; // 지표값
}
