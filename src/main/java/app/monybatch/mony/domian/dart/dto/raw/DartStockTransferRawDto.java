package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartStockTransferRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("iscmp_cmpnm")        private String iscmpCmpnm;          // 발행회사명
    @JsonProperty("trfdtl_stkcnt")      private Long trfdtlStkcnt;          // 양도주식수
    @JsonProperty("trfdtl_trfprc")      private BigDecimal trfdtlTrfprc;    // 양도금액
    @JsonProperty("trfdtl_tast")        private BigDecimal trfdtlTast;      // 총자산
    @JsonProperty("trfdtl_tast_vs")     private BigDecimal trfdtlTastVs;    // 총자산대비(%)
    @JsonProperty("attrf_owstkcnt")     private Long attrfOwstkcnt;         // 양도후 소유주식수
    @JsonProperty("attrf_eqrt")         private BigDecimal attrfEqrt;       // 양도후 지분비율(%)
    @JsonProperty("trf_pp")             private String trfPp;               // 양도목적
    @JsonProperty("trf_prd")            private String trfPrd;              // 양도예정일자
}
