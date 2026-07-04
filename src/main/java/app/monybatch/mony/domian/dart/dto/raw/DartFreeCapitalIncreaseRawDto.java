package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartFreeCapitalIncreaseRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("nstk_ostk_cnt")          private Long nstkOstkCnt;
    @JsonProperty("nstk_estk_cnt")          private Long nstkEstkCnt;
    @JsonProperty("nstk_asstd")             private String nstkAsstd;           // 신주배정기준일
    @JsonProperty("nstk_ascnt_ps_ostk")     private BigDecimal nstkAscntPsOstk; // 1주당 신주배정(보통)
    @JsonProperty("nstk_ascnt_ps_estk")     private BigDecimal nstkAscntPsEstk; // 1주당 신주배정(기타)
    @JsonProperty("nstk_lstprd")            private String nstkLstprd;          // 신주 상장예정일
    @JsonProperty("bddd")                   private String bddd;                // 이사회결의일
}
