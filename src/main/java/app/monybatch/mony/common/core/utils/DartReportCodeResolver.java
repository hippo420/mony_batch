package app.monybatch.mony.common.core.utils;

/**
 * 접수일(rceptDt, yyyyMMdd) 기준으로 DART reprt_code / bsns_year 자동 추론.
 * 12월 결산법인 기준 제출 패턴을 따른다.
 *
 * 1~3월  접수 → 사업보고서(11011),   bsns_year = 접수연도 - 1  (전년도 결산, 90일 이내)
 * 4~5월  접수 → 1분기보고서(11013),  bsns_year = 접수연도      (Q1 종료 45일 이내)
 * 7~8월  접수 → 반기보고서(11012),   bsns_year = 접수연도      (H1 종료 45일 이내)
 * 10~11월 접수 → 3분기보고서(11014), bsns_year = 접수연도      (Q3 종료 45일 이내)
 * 그 외  (6·9·12월) → 사업보고서(11011), bsns_year = 접수연도 - 1
 */
public final class DartReportCodeResolver {

    public static final String ANNUAL = "11011"; // 사업보고서
    public static final String Q1     = "11013"; // 1분기보고서
    public static final String HALF   = "11012"; // 반기보고서
    public static final String Q3     = "11014"; // 3분기보고서

    private DartReportCodeResolver() {}

    /**
     * @param rceptDt 접수일 (yyyyMMdd)
     * @return reprt_code
     */
    public static String resolveReprtCode(String rceptDt) {
        int m = month(rceptDt);
        if (m >= 4  && m <= 5)  return Q1;
        if (m >= 7  && m <= 8)  return HALF;
        if (m >= 10 && m <= 11) return Q3;
        return ANNUAL; // 1~3월, 6·9·12월
    }

    /**
     * @param rceptDt 접수일 (yyyyMMdd)
     * @return bsns_year (4자리 문자열)
     */
    public static String resolveBsnsYear(String rceptDt) {
        int year  = Integer.parseInt(rceptDt.substring(0, 4));
        int month = month(rceptDt);
        // 사업보고서(1~3월)만 전년도 데이터, 나머지는 접수연도
        return (month >= 1 && month <= 3)
                ? String.valueOf(year - 1)
                : String.valueOf(year);
    }

    private static int month(String rceptDt) {
        return Integer.parseInt(rceptDt.substring(4, 6));
    }
}
