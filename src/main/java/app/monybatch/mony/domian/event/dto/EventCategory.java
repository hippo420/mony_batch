package app.monybatch.mony.domian.event.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum EventCategory {

    MONETARY("통화정책"),
    EMPLOYMENT("고용"),
    ACTIVITY("경기활동"),
    INFLATION("물가"),
    ENERGY("에너지"),
    UNKNOWN("기타");

    private final String description;
    // 미리 Map으로 캐싱하여 성능 최적화 (O(1))
    private static final Map<String, EventCategory> DESCRIPTION_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(EventCategory::getDescription, e -> e));

    /**
     * 설명(String)을 기반으로 Enum을 찾아 반환
     */
    public static EventCategory fromDescription(String description) {
        return Optional.ofNullable(DESCRIPTION_MAP.get(description))
                .orElseThrow(() -> new IllegalArgumentException("일치하는 카테고리가 없습니다: " + description));
    }

    /**
     * 특정 키워드가 포함되어 있는지 확인하여 반환 (유연한 매칭)
     */
    public static EventCategory findByKeyword(String text) {
        return Arrays.stream(values())
                .filter(category -> text.contains(category.getDescription()))
                .findFirst()
                .orElse(null); // 못 찾으면 null 혹은 기본값 설정
    }

    @JsonCreator
    public static EventCategory from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null; // 또는 EventCategory.UNKNOWN 등 기본값 반환
        }
        return EventCategory.valueOf(value.toUpperCase());
    }
}
