package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartCapitalReductionRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("cr_rt_ostk")         private BigDecimal crRtOstk;        // 감자비율(보통주, %)
    @JsonProperty("cr_rt_estk")         private BigDecimal crRtEstk;        // 감자비율(기타주, %)
    @JsonProperty("bfcr_cpt")           private BigDecimal bfcrCpt;         // 감자전 자본금
    @JsonProperty("atcr_cpt")           private BigDecimal atcrCpt;         // 감자후 자본금
    @JsonProperty("cr_mth")             private String crMth;               // 감자방법
    @JsonProperty("cr_rs")              private String crRs;                // 감자사유
    @JsonProperty("cr_std")             private String crStd;               // 감자기준일
    @JsonProperty("crsc_gmtsck_prd")    private String crscGmtsckPrd;       // 주주총회 예정일
}
