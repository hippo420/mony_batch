package app.monybatch.mony.common.constant;

import java.util.Map;

/**
 * Job 이름 → 도메인 매핑 (batch/{domain}/job 패키지 구조와 대응).
 * 도메인별 로그 파일 분리(logback SiftingAppender)와 모니터링 API의 도메인 그룹핑에 공용으로 사용.
 */
public final class JobDomain {

    private static final Map<String, String> JOB_DOMAIN_MAP = Map.ofEntries(
            Map.entry("dartJob", "dart"),
            Map.entry("fetchDartAcctJob", "dart"),
            Map.entry("fetchDartInfoJob", "dart"),
            Map.entry("dartRssJob", "dart"),
            Map.entry("newsCollectionJob", "news"),
            Map.entry("economicEventCollectionJob", "event"),
            Map.entry("investorTradeInfoJob", "stock"),
            Map.entry("pileTradeJob", "stock"),
            Map.entry("stockAlertJob", "stock"),
            Map.entry("stockItemJob", "stock"),
            Map.entry("stockPriceJob", "stock"),
            Map.entry("reportCollectionJob", "report")
    );

    private JobDomain() {
    }

    public static String resolve(String jobName) {
        return JOB_DOMAIN_MAP.getOrDefault(jobName, "etc");
    }
}
