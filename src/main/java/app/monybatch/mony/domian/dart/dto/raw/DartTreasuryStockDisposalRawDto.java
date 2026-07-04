package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartTreasuryStockDisposalRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("dppln_stk_ostk")     private Long dpplnStkOstk;          // 처분예정주식(보통)
    @JsonProperty("dppln_stk_estk")     private Long dpplnStkEstk;          // 처분예정주식(기타)
    @JsonProperty("dppln_prc_ostk")     private BigDecimal dpplnPrcOstk;    // 처분예정금액(보통)
    @JsonProperty("dppln_prc_estk")     private BigDecimal dpplnPrcEstk;    // 처분예정금액(기타)
    @JsonProperty("dpprpd_bgd")         private String dpprpdBgd;           // 처분예정기간(시작)
    @JsonProperty("dpprpd_edd")         private String dpprpdEdd;           // 처분예정기간(종료)
    @JsonProperty("dp_pp")              private String dpPp;                // 처분목적
    @JsonProperty("dp_m_mkt")           private Long dpMMkt;                // 처분방법(시장매도)
    @JsonProperty("dp_m_ovtm")          private Long dpMOvtm;               // 처분방법(시간외대량매매)
    @JsonProperty("dp_m_otc")           private Long dpMOtc;                // 처분방법(장외처분)
    @JsonProperty("dp_m_etc")           private Long dpMEtc;                // 처분방법(기타)
    @JsonProperty("dp_dd")              private String dpDd;                // 처분결정일
}
