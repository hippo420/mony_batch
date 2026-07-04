package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class DartDividendRawDto {

    @JsonProperty("rcept_no")
    private String rceptNo;   // 접수번호

    @JsonProperty("corp_cls")
    private String corpCls;   // 법인구분

    @JsonProperty("corp_code")
    private String corpCode;  // 고유번호

    @JsonProperty("corp_name")
    private String corpName;  // 회사명

    @JsonProperty("se")
    private String se;        // 구분 (1주당 현금배당금(원) / 시가배당율(%) / 현금배당금총액(백만원))

    @JsonProperty("stock_knd")
    private String stockKnd;  // 주식 종류 (보통주식 / 우선주식)

    @JsonProperty("thstrm")
    private String thstrm;    // 당기 (문자열, 예: "1,500" / "-")

    @JsonProperty("frmtrm")
    private String frmtrm;    // 전기

    @JsonProperty("lwfr")
    private String lwfr;      // 전전기

    @JsonProperty("stlm_dt")
    private String stlmDt;    // 결산기준일 (yyyyMMdd)
}
