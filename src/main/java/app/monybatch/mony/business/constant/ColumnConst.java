package app.monybatch.mony.business.constant;

public class ColumnConst {

    /** DART*/
    /** 1.정기보고서 재무정보 */
    /** 발급받은 인증키(40자리) */
    public static final String CRTFC_KEY = "crtfc_key";
    /** 공시대상회사의 고유번호(8자리)*/
    public static final String CORP_CODE = "corp_code";
    /** 업연도(4자리) ※ 2015년 이후 부터 정보제공*/
    public static final String BSNS_YEAR = "bsns_year";
    /** 1분기보고서 : 11013, 반기보고서 : 11012, 3분기보고서 : 11014, 사업보고서 : 11011*/
    public static final String REPRT_CODE = "reprt_code";

    /**  KRX  */
    /** 한국거래소 인증키 */
    public static final String AUTH_KEY = "AUTH_KEY";
    /** 기준일자 */
    public static final String BASDD = "basDd";

}
