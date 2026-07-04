package app.monybatch.mony.domian.dart.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dart_total_stock",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dart_ts",
                        columnNames = {"rcept_no", "corp_code", "se"}
                )
        },
        indexes = {
                @Index(name = "idx_ts_corp_code", columnList = "corp_code"),
                @Index(name = "idx_ts_rcept_no", columnList = "rcept_no")
        }
)
@Getter @Setter @NoArgsConstructor
public class DartTotalStock extends DartDisclosureBase {

    @Column(name = "se", length = 100, nullable = false)
    @JsonProperty("se")
    private String se; // 구분

    @Column(name = "isu_stock_totqy")
    @JsonProperty("isu_stock_totqy")
    private Long isuStockTotqy; // 발행할 주식의 총수

    @Column(name = "now_to_isu_stock_totqy")
    @JsonProperty("now_to_isu_stock_totqy")
    private Long nowToIsuStockTotqy; // 현재까지 발행한 주식의 총수

    @Column(name = "now_to_dcrs_stock_totqy")
    @JsonProperty("now_to_dcrs_stock_totqy")
    private Long nowToDcrsStockTotqy; // 현재까지 감소한 주식의 총수

    @Column(name = "redc")
    @JsonProperty("redc")
    private Long redc; // 감자

    @Column(name = "profit_incnr")
    @JsonProperty("profit_incnr")
    private Long profitIncnr; // 이익소각

    @Column(name = "rdmstk_repy")
    @JsonProperty("rdmstk_repy")
    private Long rdmstkRepy; // 상환주식의 상환

    @Column(name = "etc")
    @JsonProperty("etc")
    private Long etc; // 기타

    @Column(name = "istc_totqy")
    @JsonProperty("istc_totqy")
    private Long istcTotqy; // 발행주식의 총수

    @Column(name = "tesstk_co")
    @JsonProperty("tesstk_co")
    private Long tesstk; // 자기주식수

    @Column(name = "distb_stock_co")
    @JsonProperty("distb_stock_co")
    private Long distbStockCo; // 유통주식수

    @Column(name = "stlm_dt", length = 8)
    @JsonProperty("stlm_dt")
    private String stlmDt; // 결산기준일
}
