package app.monybatch.mony.business.entity.report;

import java.util.Arrays;
import java.util.List;

public enum Invest {
    BUY("BY", Arrays.asList("BUY", "매수", "STRONG BUY", "OUTPERFORM", "적극매수")),
    SELL("SL", Arrays.asList("SELL", "매도", "UNDERPERFORM", "축소")),
    NEUTRAL("NT", Arrays.asList("NEUTRAL", "중립", "HOLD", "MARKETPERFORM"));
    ;
    private final String code;
    private final List<String> keywords; // 증권사별 표현들을 담는 리스트

    Invest(String code, List<String> keywords) {
        this.code = code;
        this.keywords = keywords;
    }

    public String getCode() {
        return code;
    }

    /**
     * 입력받은 텍스트(증권사 의견)를 분석하여 표준화된 코드를 반환
     * @param inputText 증권사 리포트에서 추출한 투자의견 (예: "매수", "Hold")
     * @return "BY", "SL", "NT" 또는 매칭되지 않을 경우 "UNKNOWN"
     */
    public static String findCode(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return "UNKNOWN";
        }

        // 대소문자 구분 없이 비교하기 위해 대문자로 변환 및 공백 제거
        String cleanInput = inputText.toUpperCase().trim();

        return Arrays.stream(Invest.values())
                .filter(invest -> invest.keywords.stream()
                        .anyMatch(keyword -> cleanInput.contains(keyword)))
                .map(Invest::getCode)
                .findFirst()
                .orElse("NT"); // 매칭되는 게 없으면 보통 '중립'으로 처리하거나 "UNKNOWN" 반환
    }

}
