package app.monybatch.mony.batch.event.parser;

import app.monybatch.mony.domian.event.dto.EventCategory;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EconomicCategoryClassifier {

    // 각 카테고리별 패턴 정의 (기존과 동일)
    private static final Map<EventCategory, Pattern> PATTERNS = new EnumMap<>(EventCategory.class);

    static {
        PATTERNS.put(EventCategory.MONETARY, Pattern.compile("(연준|연방준비은행|fed|fomc|금리|채권|국채|매입|대차대조표|연설)", Pattern.CASE_INSENSITIVE));
        PATTERNS.put(EventCategory.EMPLOYMENT, Pattern.compile("(고용|실업|JOLT|ADP|비농장|급여|실업수당)", Pattern.CASE_INSENSITIVE));
        PATTERNS.put(EventCategory.ACTIVITY, Pattern.compile("(PMI|소매 판매|GDP|산업 생산|주문|재고|소비자 신뢰)", Pattern.CASE_INSENSITIVE));
        PATTERNS.put(EventCategory.INFLATION, Pattern.compile("(CPI|PPI|인플레이션|물가|주택가격|케이스실러|소비자 물가)", Pattern.CASE_INSENSITIVE));
        PATTERNS.put(EventCategory.ENERGY, Pattern.compile("(원유|EIA|재고|곡물|옥수수|대두|밀|가솔린|천연가스)", Pattern.CASE_INSENSITIVE));
    }

    public static EventCategory classify(String eventName) {
        if (eventName == null || eventName.isBlank()) {
            return EventCategory.UNKNOWN;
        }

        EventCategory bestCategory = EventCategory.UNKNOWN;
        int maxScore = 0;

        // 각 카테고리별로 점수 계산
        for (Map.Entry<EventCategory, Pattern> entry : PATTERNS.entrySet()) {
            int currentScore = countMatches(entry.getValue(), eventName);

            // 더 높은 점수를 가진 카테고리로 업데이트
            if (currentScore > maxScore) {
                maxScore = currentScore;
                bestCategory = entry.getKey();
            }
        }

        return bestCategory;
    }

    // 특정 패턴이 문자열 내에서 몇 번 발생하는지 계산하는 메서드
    private static int countMatches(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}