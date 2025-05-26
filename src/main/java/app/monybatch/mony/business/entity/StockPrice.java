package app.monybatch.mony.business.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "STOCK_PRICE")
@IdClass(StockPriceId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice {
    @Id
    @Column(name = "bas_dd", length = 8)
    @JsonProperty("BAS_DD")
    private String basDd;

    @Id
    @Column(name = "isu_cd", length = 20)
    @JsonProperty("ISU_CD")
    private String isuCd;

    @Column(name = "isu_nm")
    @JsonProperty("ISU_NM")
    private String isuNm;

    @Column(name = "acc_trdval", precision = 38, scale = 2)
    @JsonProperty("ACC_TRDVAL")
    private BigDecimal accTrdval;

    @Column(name = "acc_trdvol")
    @JsonProperty("ACC_TRDVOL")
    private Long accTrdvol;

    @Column(name = "cmpprevdd_prc")
    @JsonProperty("CMPPREVDD_PRC")
    private Long cmpprevddPrc;

    @Column(name = "fluc_rt", precision = 38, scale = 2)
    @JsonProperty("FLUC_RT")
    private BigDecimal flucRt;

    @Column(name = "list_shrs")
    @JsonProperty("LIST_SHRS")
    private Long listShrs;

    @Column(name = "mktcap", precision = 38, scale = 2)
    @JsonProperty("MKTCAP")
    private BigDecimal mktcap;

    @Column(name = "mkt_nm")
    @JsonProperty("MKT_NM")
    private String mktNm;

    @Column(name = "sect_tp_nm")
    @JsonProperty("SECT_TP_NM")
    private String sectTpNm;

    @Column(name = "tdd_clsprc")
    @JsonProperty("TDD_CLSPRC")
    private Long tddClsprc;

    @Column(name = "tdd_hgprc", precision = 38, scale = 2)
    @JsonProperty("TDD_HGPRC")
    private BigDecimal tddHgprc;

    @Column(name = "tdd_lwprc", precision = 38, scale = 2)
    @JsonProperty("TDD_LWPRC")
    private BigDecimal tddLwprc;

    @Column(name = "tdd_opnprc", precision = 38, scale = 2)
    @JsonProperty("TDD_OPNPRC")
    private BigDecimal tddOpnprc;
}
