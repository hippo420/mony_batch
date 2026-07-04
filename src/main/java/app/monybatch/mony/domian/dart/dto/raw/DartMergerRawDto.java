package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartMergerRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("mg_mth")             private String mgMth;               // 합병방법
    @JsonProperty("mg_rt")              private String mgRt;                // 합병비율
    @JsonProperty("mg_pp")              private String mgPp;                // 합병목적
    @JsonProperty("mgptncmp_cmpnm")     private String mgptncmpCmpnm;       // 합병상대회사명
    @JsonProperty("rbsnfdtl_tast")      private BigDecimal rbsnfdtlTast;    // 합병상대 자산총계
    @JsonProperty("rbsnfdtl_nic")       private BigDecimal rbsnfdtlNic;     // 합병상대 당기순이익
    @JsonProperty("bdlst_atn")          private String bdlstAtn;            // 우회상장 해당여부
    @JsonProperty("mgsc_mgdt")          private String mgscMgdt;            // 합병기일
    @JsonProperty("mgsc_gmtsck_prd")    private String mgscGmtsckPrd;       // 주주총회 예정일
    @JsonProperty("mgsc_nstklstprd")    private String mgscNstklstprd;      // 신주 상장예정일
}
