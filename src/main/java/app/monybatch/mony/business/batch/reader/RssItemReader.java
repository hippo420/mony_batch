package app.monybatch.mony.business.batch.reader;

import app.monybatch.mony.business.entity.news.News;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RssItemReader implements ItemReader<News> {

    private final String rssUrl;
    private Iterator<SyndEntry> itemIterator;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;


    public RssItemReader(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    @Override
    public News read() throws Exception {
        if (itemIterator == null) {
            // URL 연결 및 스트림 열기
            URLConnection connection = new URL(rssUrl).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0"); // 일부 RSS는 User-Agent 필요
            try (InputStream inputStream = connection.getInputStream()) {
                // 스트림을 문자열로 읽기
                String xmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                // 유효하지 않은 XML 문자 제거 (정규식 사용)
                String sanitizedXml = xmlContent.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");

                // 정제된 문자열로 피드 파싱
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new StringReader(sanitizedXml));

                List<SyndEntry> entries = feed.getEntries();
                this.itemIterator = entries.iterator();
                log.info("Reading RSS feed from: {}, Found {} articles.", rssUrl, entries.size());
            } catch (Exception e) {
                log.error("Failed to read or parse RSS feed: {}", rssUrl, e);
                // 오류 발생 시 빈 리스트로 초기화하여 다음 Reader로 넘어가도록 함
                this.itemIterator = Collections.emptyIterator();
            }
        }

        if (itemIterator.hasNext()) {
            SyndEntry entry = itemIterator.next();
            return parseEntry(entry);
        } else {
            return null; // End of feed
        }
    }

    private News parseEntry(SyndEntry entry) {
        News news = new News();

        // 제목 정제
        if (entry.getTitle() != null) {
            news.setTitle(cleanText(entry.getTitle()));
        }

        news.setOriginallink(entry.getLink());

        // 설명(Description) 정제
        if (entry.getDescription() != null) {
            news.setDescription(cleanText(entry.getDescription().getValue()));
        } else {
            news.setDescription("");
        }


        if (entry.getPublishedDate() != null) {
            String isoDate = entry.getPublishedDate().toInstant()
                    .atZone(ZoneId.systemDefault()) // 시스템 기본 시간대로 변환
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            news.setPubDate(isoDate);
        }


        // 언론사별 특화 파싱
        if (rssUrl.contains("news.google.com")) {
            // Google News는 description에 HTML 목록이 포함됨
            // 여기서는 단순 텍스트로 저장하지만, 필요시 추가 파싱 가능
        } else if (rssUrl.contains("jtbc.co.kr")) {
            // JTBC는 CDATA로 감싸져 있지만 Rome이 자동으로 처리
        } else if (rssUrl.contains("yna.co.kr")) {
            // 연합뉴스는 dc:creator (작성자) 정보가 있음
            // news.setAuthor(entry.getAuthor()); // Rome이 author를 파싱
        } else if (rssUrl.contains("sbs.co.kr")) {
            // SBS는 author 태그에 이메일과 이름이 같이 있음
            // news.setAuthor(entry.getAuthor());
        } else if (rssUrl.contains("thevaluenews.co.kr")) {
            // 더벨
        } else if (rssUrl.contains("wowtv.co.kr")) {
            // 한국경제
        }
        return news;
    }

    private String cleanText(String text) {
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
