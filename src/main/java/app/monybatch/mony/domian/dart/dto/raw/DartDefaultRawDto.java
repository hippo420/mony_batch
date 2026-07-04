package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartDefaultRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("df_amt")     private BigDecimal dfAmt;   // 부도금액
    @JsonProperty("df_bnk")     private String dfBnk;       // 부도발생은행
    @JsonProperty("df_rs")      private String dfRs;        // 부도사유
    @JsonProperty("dfd")        private String dfd;         // 최종부도일자
}
