package app.monybatch.mony.business.constant;

public class ReportConst {

    public static final String NAVER_REPORT_URL="https://finance.naver.com/research/company_list.naver?keyword=&brokerCode=&searchType=writeDate&writeFromDate=%s&writeToDate=%s&itemName=&itemCode=&x=35&y=19&page=%d";

    public static final String NAVER_REPORT_DETAIL_URL ="https://finance.naver.com/research/%s&searchType=writeDate&writeFromDate=%s&writeToDate=%s";

    public static final String MINIO_URL_REPORTS = "http://localhost:7005/reports/%s";
}
