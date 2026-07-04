package app.monybatch.mony.domian.dart.entity;

import java.util.List;

public enum DisclosureType {

    // 선언 순서 = 매칭 우선순위 (DisclosureTypeClassifier가 순서대로 검사)
    // 자기주식 취득/처분은 "취득"/"처분" 단독 키워드보다 먼저 위치
    // useReportCode = true  → bsns_year + reprt_code 파라미터 (정기 보고서 기반 API)
    // useReportCode = false → bgn_de + end_de 파라미터 (이벤트 공시 기반 API)

    TREASURY_ACQUISITION("자기주식 취득", "/api/tesstkAqDecsn.json", "DartTreasuryStockAcquisition",
            List.of("자기주식취득", "자기주식의취득"), false),
    TREASURY_DISPOSAL("자기주식 처분", "/api/tesstkDpDecsn.json", "DartTreasuryStockDisposal",
            List.of("자기주식처분", "자기주식의처분"), false),

    PAID_CAPITAL_INCREASE("유상증자", "/api/irdsSttus.json", "DartPaidCapitalIncrease",
            List.of("유상증자"), false),
    FREE_CAPITAL_INCREASE("무상증자", "/api/freeIssueOfStkSttus.json", "DartFreeCapitalIncrease",
            List.of("무상증자"), false),
    CAPITAL_REDUCTION("감자", "/api/crTrmsRs.json", "DartCapitalReduction",
            List.of("감자"), false),

    TANGIBLE_ASSET_ACQUISITION("유형자산 양수", "/api/tgastInhboundDecsn.json", "DartTangibleAssetAcquisition",
            List.of("유형자산양수"), false),
    STOCK_ACQUISITION("주식 양수", "/api/stkInhboundDecsn.json", "DartStockAcquisition",
            List.of("주식양수", "출자증권양수"), false),
    STOCK_TRANSFER("주식 양도", "/api/stkOtboundDecsn.json", "DartStockTransfer",
            List.of("주식양도", "출자증권양도"), false),

    MERGER("합병", "/api/mgDecsn.json", "DartMerger",
            List.of("합병"), false),
    SPIN_OFF("분할", "/api/dvDecsn.json", "DartSpinOff",
            List.of("분할"), false),
    DEFAULT("부도", "/api/dfOcr.json", "DartDefault",
            List.of("부도발생"), false),
    BUSINESS_SUSPENSION("영업정지", "/api/bsnSspn.json", "DartBusinessSuspension",
            List.of("영업정지"), false),
    REHABILITATION("회생절차", "/api/ctrcvsBgrq.json", "DartRehabilitation",
            List.of("회생절차", "법정관리"), false),
    DISSOLUTION("해산", "/api/dsOcr.json", "DartDissolution",
            List.of("해산사유"), false),
    LAWSUIT("소송", "/api/lwstSttus.json", "DartLawsuit",
            List.of("소송"), false),

    BAEDANG("배당", "/api/alotMatter.json", "DartDividend",
            List.of("배당결정", "현금배당", "현물배당"), true),

    // 영업(잠정)실적 공시: JSON API가 아니라 원본 문서(document.xml)를 받아
    // ZIP 해제 → 보고서 XML 파싱 → LLM 요약하는 별도 파이프라인을 탄다.
    // (DartDisclosureConsumer 가 type 으로 분기하여 PerformanceDisclosureHandler 로 위임)
    BUSINESS_PERFORMANCE("영업(잠정)실적", "/api/document.xml", "DartPerformance",
            List.of("(잠정)실적", "잠정실적", "영업실적"), false),

    UNKNOWN("알 수 없음", "", "스킵", List.of(), false);

    private final String description;
    private final String apiPath;
    private final String className;
    private final List<String> keywords;
    private final boolean useReportCode; // true: bsns_year+reprt_code / false: bgn_de+end_de

    DisclosureType(String description, String apiPath, String className,
                   List<String> keywords, boolean useReportCode) {
        this.description   = description;
        this.apiPath       = apiPath;
        this.className     = className;
        this.keywords      = keywords;
        this.useReportCode = useReportCode;
    }

    public String getDescription()    { return description; }
    public String getApiPath()        { return apiPath; }
    public String getClassName()      { return className; }
    public List<String> getKeywords() { return keywords; }
    public boolean isUseReportCode()  { return useReportCode; }

    public static DisclosureType findByApiPath(String apiPath) {
        for (DisclosureType type : values()) {
            if (type.apiPath.equals(apiPath)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
