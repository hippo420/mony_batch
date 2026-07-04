package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartTangibleAssetAcquisitionRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("ast_sen")            private String astSen;              // 자산구분
    @JsonProperty("ast_nm")             private String astNm;               // 자산명
    @JsonProperty("inhdtl_inhprc")      private BigDecimal inhdtlInhprc;    // 양수금액
    @JsonProperty("inhdtl_tast")        private BigDecimal inhdtlTast;      // 자산총액
    @JsonProperty("inhdtl_tast_vs")     private BigDecimal inhdtlTastVs;    // 자산총액대비(%)
    @JsonProperty("inh_pp")             private String inhPp;               // 양수목적
    @JsonProperty("inh_af")             private String inhAf;               // 양수영향
    @JsonProperty("inh_prd_ctr_cnsd")   private String inhPrdCtrCnsd;       // 계약체결일
    @JsonProperty("inh_prd_inh_std")    private String inhPrdInhStd;        // 양수기준일
}
