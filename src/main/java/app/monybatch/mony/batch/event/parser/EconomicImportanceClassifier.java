package app.monybatch.mony.batch.event.parser;

import app.monybatch.mony.domian.event.dto.MarketImpact;

import java.util.regex.Pattern;

public class EconomicImportanceClassifier {
    // =========================
    // HIGH
    // =========================
    private static final Pattern HIGH_PATTERN = Pattern.compile(
            ".*(" +
                    "금리|FOMC|연준|파월|Fed.*연설|" +
                    "CPI|PCE|인플레이션|물가|" +
                    "비 농장 급여|NFP|실업률|" +
                    "GDP" +
                    ").*",
            Pattern.CASE_INSENSITIVE
    );

    // =========================
    // MEDIUM
    // =========================
    private static final Pattern MEDIUM_PATTERN = Pattern.compile(
            ".*(" +
                    "PMI|소매 판매|소비자 신뢰지수|" +
                    "ADP|JOLT|고용변화|" +
                    "기업재고|내구재 주문|" +
                    "국채 입찰|채권 경매" +
                    ").*",
            Pattern.CASE_INSENSITIVE
    );

    // =========================
    // LOW
    // =========================
    private static final Pattern LOW_PATTERN = Pattern.compile(
            ".*(" +
                    "EIA|API|원유|재고|" +
                    "곡물|옥수수|밀|대두|" +
                    "천연가스|" +
                    "지역 연준|필라델피아|캔자스|NY Fed" +
                    ").*",
            Pattern.CASE_INSENSITIVE
    );

    public static MarketImpact classify(String eventName, String category) {

        String text = (eventName + " " + category).toLowerCase();

        // 1. HIGH 우선
        if (HIGH_PATTERN.matcher(text).find()) {
            return MarketImpact.HIGH;
        }

        // 2. MEDIUM
        if (MEDIUM_PATTERN.matcher(text).find()) {
            return MarketImpact.MEDIUM;
        }

        // 3. LOW
        if (LOW_PATTERN.matcher(text).find()) {
            return MarketImpact.LOW;
        }

        // 4. 카테고리 fallback
        return fallbackByCategory(category);
    }

    private static MarketImpact fallbackByCategory(String category) {
        if (category == null) return MarketImpact.LOW;

        return switch (category.toUpperCase()) {
            case "MONETARY" -> MarketImpact.HIGH;
            case "INFLATION" -> MarketImpact.HIGH;
            case "EMPLOYMENT" -> MarketImpact.HIGH;
            case "ACTIVITY" -> MarketImpact.MEDIUM;
            case "ENERGY" -> MarketImpact.LOW;
            default -> MarketImpact.LOW;
        };
    }
}
