package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class DartLawsuitRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("icnm")       private String icnm;    // 사건의 명칭
    @JsonProperty("ac_ap")      private String acAp;    // 원고·신청인
    @JsonProperty("rq_cn")      private String rqCn;    // 청구내용
    @JsonProperty("cpct")       private String cpct;    // 관할법원
    @JsonProperty("lgd")        private String lgd;     // 제기일자
}
