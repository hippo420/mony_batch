package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class DartSpinOffRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("dv_mth")                     private String dvMth;               // 분할방법
    @JsonProperty("dv_rt")                      private String dvRt;                // 분할비율
    @JsonProperty("atdv_excmp_cmpnm")           private String atdvExcmpCmpnm;      // 존속회사명
    @JsonProperty("atdv_excmp_atdv_lstmn_atn")  private String atdvExcmpLstmnAtn;   // 존속회사 상장유지여부
    @JsonProperty("dvfcmp_cmpnm")               private String dvfcmpCmpnm;         // 분할설립회사명
    @JsonProperty("dvfcmp_rlst_atn")            private String dvfcmpRlstAtn;       // 재상장신청 여부
    @JsonProperty("dvdt")                       private String dvdt;                // 분할기일
    @JsonProperty("gmtsck_prd")                 private String gmtsckPrd;           // 주주총회 예정일
}
