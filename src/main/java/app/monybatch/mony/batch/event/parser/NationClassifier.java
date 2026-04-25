package app.monybatch.mony.batch.event.parser;

import java.util.HashMap;
import java.util.Map;

public class NationClassifier {
    private static final Map<String, String> NATION_CODES = new HashMap<>();

    static {
        // 자주 사용되는 국가들 정의
        NATION_CODES.put("SOUTH KOREA", "KR");
        NATION_CODES.put("KOREA", "KR");
        NATION_CODES.put("UNITED STATES", "US");
        NATION_CODES.put("USA", "US");
        NATION_CODES.put("JAPAN", "JP");
        NATION_CODES.put("CHINA", "CN");
        // 필요에 따라 추가...
    }

    public static String convertToCode(String englishName) {
        if (englishName == null) return null;

        String upperName = englishName.trim().toUpperCase();
        return NATION_CODES.getOrDefault(upperName, "UNKNOWN");
    }

}