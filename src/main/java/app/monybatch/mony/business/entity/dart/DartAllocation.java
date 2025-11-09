package app.monybatch.mony.business.entity.dart;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Getter
@Setter
@Table(name="MAST_ALLOCATION")
public class DartAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ID;

    //법인구분
    @JsonProperty("corp_cls")
    @Column(updatable = false)
    private String CORP_CLS;

    // 고유번호
    @Column(updatable = false)
    @JsonProperty("corp_code")
    private String CORP_CODE;

    // 법인명
    @JsonProperty("corp_name")
    private String CORP_NAME;

    // 구분
    @JsonProperty("se")
    private String SE;

    // 주식 종류
    @JsonProperty("stock_knd")
    private String STOCK_KND;

    //결산기준일
    @JsonProperty("stlm_dt")
    private Date STLM_DT;

    // 당기
    @JsonProperty("thstrm")
    private BigDecimal thstrm;

    // 전기
    @JsonProperty("frmtrm")
    private BigDecimal FRMTRM;

    // 전전기
    @JsonProperty("lwfr")
    private BigDecimal LWFR;


}
