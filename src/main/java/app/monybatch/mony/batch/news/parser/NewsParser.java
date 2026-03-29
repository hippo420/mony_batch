package app.monybatch.mony.batch.news.parser;

import app.monybatch.mony.domian.news.entity.NewsDto;
import com.rometools.rome.feed.synd.SyndEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface NewsParser {
    void parseSpecific(SyndEntry entry, NewsDto news);
    boolean isSupported(String rssUrl);

    default String cleanText(String text) {
        if (text == null) return "";

        // 1. HTML 태그 제거
        String cleaned = text.replaceAll("<[^>]*>", "");

        // 2. HTML 엔티티 디코딩 (기본)
        cleaned = cleaned.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&nbsp;", " ");

        // 3. 숫자형 HTML 엔티티 디코딩 (&#39; 등)
        Pattern pattern = Pattern.compile("&#(\\d+);");
        Matcher matcher = pattern.matcher(cleaned);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            try {
                int codePoint = Integer.parseInt(matcher.group(1));
                matcher.appendReplacement(sb, new String(Character.toChars(codePoint)));
            } catch (IllegalArgumentException e) {
                // 유효하지 않은 코드 포인트는 그대로 둠
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        cleaned = sb.toString();

        // 4. 16진수 HTML 엔티티 디코딩 (&#x27; 등)
        pattern = Pattern.compile("&#x([0-9a-fA-F]+);");
        matcher = pattern.matcher(cleaned);
        sb = new StringBuilder();
        while (matcher.find()) {
            try {
                int codePoint = Integer.parseInt(matcher.group(1), 16);
                matcher.appendReplacement(sb, new String(Character.toChars(codePoint)));
            } catch (IllegalArgumentException e) {
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        cleaned = sb.toString();

        // 5. 특수 따옴표 및 문자 정규화
        cleaned = cleaned.replace('’', '\'') // right single quote
                .replace('‘', '\'') // left single quote
                .replace('“', '"')  // left double quote
                .replace('”', '"'); // right double quote

        // 6. 앞뒤 공백 및 연속 공백 제거
        return cleaned.trim().replaceAll("\\s+", " ");
    }
}
