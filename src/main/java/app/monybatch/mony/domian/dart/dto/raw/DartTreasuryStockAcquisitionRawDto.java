package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartTreasuryStockAcquisitionRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("aqpln_stk_ostk")         private Long aqplnStkOstk;          // 취득예정주식(보통)
    @JsonProperty("aqpln_stk_estk")         private Long aqplnStkEstk;          // 취득예정주식(기타)
    @JsonProperty("aqpln_prc_ostk")         private BigDecimal aqplnPrcOstk;    // 취득예정금액(보통)
    @JsonProperty("aqpln_prc_estk")         private BigDecimal aqplnPrcEstk;    // 취득예정금액(기타)
    @JsonProperty("aqexpd_bgd")             private String aqexpdBgd;           // 취득예상기간(시작)
    @JsonProperty("aqexpd_edd")             private String aqexpdEdd;           // 취득예상기간(종료)
    @JsonProperty("aq_pp")                  private String aqPp;                // 취득목적
    @JsonProperty("aq_mth")                 private String aqMth;               // 취득방법
    @JsonProperty("aq_wtn_div_ostk_rt")     private BigDecimal aqWtnDivOstkRt;  // 취득전 보유비율(배당가능)
    @JsonProperty("eaq_ostk_rt")            private BigDecimal eaqOstkRt;       // 취득전 보유비율(기타취득)
    @JsonProperty("aq_dd")                  private String aqDd;                // 취득결정일
}
