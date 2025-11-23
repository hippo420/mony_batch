package app.monybatch.mony.business.constant;

public class StockConstant {
    public static String NPS_BASE_URL = "https://api.odcloud.kr/api";
    public static String NPS_PATH     = "/15071593/v1/uddi:c12fc3e9-2070-49b1-8f8b-e4a55ed92c63";

    public static String DOMESTIC_STOCK_BASE_URL = "http://1.237.59.170:3100";
    public static String DOMESTIC_STOCK_PATH     = "/krx/stocks";
    public static String DOMESTIC_STOCK_KEY;

    public static String KSD_STOCK_BASE_URL = "/svc/sample/apis/sto";
    public static String KSD_STOCK_PATH     = "/stk_isu_base_info";
    //KRX
    public static String KRX_API_KEY;
    public static String KRX_STOCK_URL = "https://data-dbg.krx.co.kr";
    public static String KRX_STOCK_TRX_URL = "https://data-dbg.krx.co.kr/svc/apis/sto/stk_bydd_trd";
    public static String MAX_9999 ="9999";

    //DART
    public static String DART_KEY;
    public static String DART_API_URL = "https://opendart.fss.or.kr";

    //New
    public static String NAVER_NEWS_API_ID;
    public static String NAVER_NEWS_API_KEY;
    public static String NAVER_NEWS_API_URL = "https://openapi.naver.com/v1/search/news.json";

    //LLM
    public static String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    public static String GEMINI_API_KEY;

}
