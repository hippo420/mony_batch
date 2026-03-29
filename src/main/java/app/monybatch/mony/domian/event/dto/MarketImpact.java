package app.monybatch.mony.domian.event.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum MarketImpact {

    LOW(1, "낮음"),
    MEDIUM(2, "보통"),
    HIGH(3, "높음");

    private final int level;
    private final String description;

    // 조회를 위한 캐시 Map 생성 (성능 최적화)
    private static final Map<Integer, MarketImpact> LEVEL_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(MarketImpact::getLevel, Function.identity()));

    private static final Map<String, MarketImpact> DESCRIPTION_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(MarketImpact::getDescription, Function.identity()));

    /**
     * 숫자 레벨(1, 2, 3)로 Enum 찾기
     */
    public static MarketImpact fromLevel(int level) {
        return LEVEL_MAP.getOrDefault(level, LOW); // 없으면 기본값 LOW
    }

    /**
     * 설명 문자열("낮음", "보통", "높음")으로 Enum 찾기
     */
    public static MarketImpact fromDescription(String description) {
        return DESCRIPTION_MAP.getOrDefault(description, LOW);
    }

    /**
     * 특정 텍스트에 중요도 키워드가 포함되어 있는지 확인 (파싱용)
     */
    public static MarketImpact findInText(String text) {
        if (text == null) return LOW;
        if (text.contains("높음") || text.contains("High")) return HIGH;
        if (text.contains("보통") || text.contains("Medium")) return MEDIUM;
        return LOW;
    }
}
