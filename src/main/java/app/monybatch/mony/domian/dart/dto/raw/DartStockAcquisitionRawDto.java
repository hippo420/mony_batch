package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartStockAcquisitionRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("iscmp_cmpnm")        private String iscmpCmpnm;          // 발행회사명
    @JsonProperty("inhdtl_stkcnt")      private Long inhdtlStkcnt;          // 양수주식수
    @JsonProperty("inhdtl_inhprc")      private BigDecimal inhdtlInhprc;    // 양수금액
    @JsonProperty("inhdtl_tast")        private BigDecimal inhdtlTast;      // 총자산
    @JsonProperty("inhdtl_tast_vs")     private BigDecimal inhdtlTastVs;    // 총자산대비(%)
    @JsonProperty("atinh_owstkcnt")     private Long atinhOwstkcnt;         // 양수후 소유주식수
    @JsonProperty("atinh_eqrt")         private BigDecimal atinhEqrt;       // 양수후 지분비율(%)
    @JsonProperty("inh_pp")             private String inhPp;               // 양수목적
    @JsonProperty("inh_prd")            private String inhPrd;              // 양수예정일자
    @JsonProperty("bdlst_atn")          private String bdlstAtn;            // 우회상장 해당여부
}
