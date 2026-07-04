package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class DartRehabilitationRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("apcnt")      private String apcnt;   // 신청인
    @JsonProperty("cpct")       private String cpct;    // 관할법원
    @JsonProperty("rq_rs")      private String rqRs;    // 신청사유
    @JsonProperty("rqd")        private String rqd;     // 신청일자
}
