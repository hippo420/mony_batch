package app.monybatch.mony.business.entity.dart;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Entity
@Getter
@Setter
@Table(name="MAST_ACCT_DATA")
public class DartFinData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ID;

    @JsonProperty("corp_code")
    @Column(updatable = false)
    private String CORP_CODE;
    // 회사 고유 코드
    @Column(updatable = false)
    @JsonProperty("stock_code")
    private String STOCK_CODE;       // 종목코드

    @JsonProperty("rcept_no")
    @Column(nullable = false, length = 10)
    private String RCEPT_NO;         // 접수번호

    @JsonProperty("reprt_code")
    private String REPRT_CODE;       // 보고서 코드 (예: 11011)

    @JsonProperty("bsns_year")
    private String BSNS_YEAR;        // 사업연도

    @JsonProperty("fs_div")
    private String FS_DIV;           // 재무제표 구분 (CFS/별도)

    @JsonProperty("fs_nm")
    private String FS_NM;            // 재무제표 이름

    @JsonProperty("sj_div")
    private String SJ_DIV;           // 계정과목 구분 코드

    @JsonProperty("sj_nm")
    private String SJ_NM;            // 계정과목 구분 이름

    @JsonProperty("account_nm")
    private String ACCOUNT_NM;       // 계정명 (예: 자본금, 자산 등)

    @JsonProperty("thstrm_nm")
    private String THSTRM_NM;        // 당기 명칭 (예: 제 25기)

    @JsonProperty("thstrm_dt")
    private String THSTRM_DT;        // 당기 기준일

    @JsonProperty("thstrm_amount")
    private String THSTRM_AMOUNT;    // 당기 금액

    @JsonProperty("frmtrm_nm")
    private String FRMTRM_NM;        // 전기 명칭

    @JsonProperty("frmtrm_dt")
    private String FRMTRM_DT;        // 전기 기준일

    @JsonProperty("frmtrm_amount")
    private String FRMTRM_AMOUNT;    // 전기 금액

    @JsonProperty("bfefrmtrm_nm")
    private String BFEFRMTRM_NM;     // 전전기 명칭

    @JsonProperty("bfefrmtrm_dt")
    private String BFEFRMTRM_DT;     // 전전기 기준일

    @JsonProperty("bfefrmtrm_amount")
    private String BFEFRMTRM_AMOUNT; // 전전기 금액

    @JsonProperty("ord")
    private Integer ORD;              // 정렬 순서

    @JsonProperty("currency")
    private String CURRENCY;         // 통화 단위 (예: 원)

}
