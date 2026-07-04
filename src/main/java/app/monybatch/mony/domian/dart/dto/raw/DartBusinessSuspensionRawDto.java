package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartBusinessSuspensionRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("bsnsp_amt")  private BigDecimal bsnspAmt;    // 영업정지금액
    @JsonProperty("sl_vs")      private BigDecimal slVs;        // 매출액 대비(%)
    @JsonProperty("bsnsp_rs")   private String bsnspRs;         // 영업정지사유
    @JsonProperty("bsnspd")     private String bsnspd;          // 영업정지일자
}
