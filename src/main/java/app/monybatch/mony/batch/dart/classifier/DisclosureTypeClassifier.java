package app.monybatch.mony.batch.dart.classifier;

import app.monybatch.mony.domian.dart.entity.DisclosureType;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DisclosureTypeClassifier {

    // [기재정정], [첨부추가], [첨부정정] 등 연속된 브라켓 접두어
    private static final Pattern BRACKET_PREFIX = Pattern.compile("^(\\[.*?])+");
    // 주요사항보고서(X) 에서 X 추출
    private static final Pattern MAJOR_REPORT = Pattern.compile("주요사항보고서\\((.+?)\\)");

    public DisclosureType classify(String reportNm) {
        if (reportNm == null || reportNm.isBlank()) {
            return DisclosureType.UNKNOWN;
        }

        String normalized = normalize(reportNm);

        for (DisclosureType type : DisclosureType.values()) {
            if (type == DisclosureType.UNKNOWN) continue;
            for (String keyword : type.getKeywords()) {
                if (normalized.contains(keyword)) {
                    return type;
                }
            }
        }

        return DisclosureType.UNKNOWN;
    }

    private String normalize(String reportNm) {
        String result = reportNm;

        // 1. [기재정정] 등 브라켓 접두어 제거
        result = BRACKET_PREFIX.matcher(result).replaceAll("").trim();

        // 2. 주요사항보고서(X) 패턴이면 괄호 안 내용만 추출
        Matcher matcher = MAJOR_REPORT.matcher(result);
        if (matcher.find()) {
            result = matcher.group(1);
        }

        // 3. 공백 전체 제거
        return result.replaceAll("\\s+", "");
    }
}