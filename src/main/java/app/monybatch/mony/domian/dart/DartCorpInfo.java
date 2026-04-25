package app.monybatch.mony.domian.dart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DartCorpInfo {

    /**
     * 상장회사 종목코드 (6자리)
     * 예: 005930
     */
    @JsonProperty("stock_code")
    private String stockCode;

    @JsonProperty("stock_name")
    private String stockName;

    @JsonProperty("corp_code")
    private String corpCode;
    /**
     * 대표자명
     * 예: 한종희
     */
    @JsonProperty("ceo_nm")
    private String ceoNm;

    /**
     * 법인구분
     * Y : 유가증권시장(KOSPI)
     * K : 코스닥(KOSDAQ)
     * N : 코넥스(KONEX)
     * E : 기타법인
     */
    @JsonProperty("corp_cls")
    private String corpCls;

    /**
     * 법인등록번호
     */
    @JsonProperty("jurir_no")
    private String jurirNo;

    /**
     * 사업자등록번호
     */
    @JsonProperty("bizr_no")
    private String bizrNo;

    /**
     * 회사 주소
     */
    @JsonProperty("adres")
    private String adres;

    /**
     * 회사 홈페이지 URL
     */
    @JsonProperty("hm_url")
    private String hmUrl;

    /**
     * IR 홈페이지 URL
     */
    @JsonProperty("ir_url")
    private String irUrl;

    /**
     * 대표 전화번호
     */
    @JsonProperty("phn_no")
    private String phnNo;

    /**
     * 팩스번호
     */
    @JsonProperty("fax_no")
    private String faxNo;

    /**
     * 업종코드
     * (KRX 또는 DART 기준 산업코드)
     */
    @JsonProperty("induty_code")
    private String indutyCode;

    /**
     * 설립일
     * 형식: YYYYMMDD
     * 예: 19690113
     */
    @JsonProperty("est_dt")
    private String estDt;

    /**
     * 결산월
     * 형식: MM
     * 예: 12
     */
    @JsonProperty("acc_mt")
    private String accMt;
}
