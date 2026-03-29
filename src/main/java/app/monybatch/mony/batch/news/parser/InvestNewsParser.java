package app.monybatch.mony.batch.news.parser;

import app.monybatch.mony.domian.news.entity.NewsDto;
import com.rometools.rome.feed.synd.SyndEntry;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InvestNewsParser implements NewsParser {
    @Override
    public void parseSpecific(SyndEntry entry, NewsDto news) {
        if (entry.getTitle() != null) {
            //log.info("전체:{}, -위치:{}",entry.getTitle().length(),entry.getTitle().lastIndexOf("-"));
            String title = entry.getTitle();
            if(title.contains(" - "))
            {
                title = title.substring(0,title.lastIndexOf("-"));
            }
            news.setTitle(cleanText(title));
        }

        news.setOriginallink(entry.getLink());
        // 설명(Description) 정제
        if (entry.getDescription() != null) {
            news.setDescription(cleanText(entry.getDescription().getValue()));
        } else {
            String content = crawlContent(entry.getLink());
            if(content.isBlank())
            {
                content = parseContent(news.getOriginallink());
            }

            //log.info("content: {}",content);
            news.setDescription(content);
        }


        if (entry.getPublishedDate() != null) {
            String isoDate = entry.getPublishedDate().toInstant()
                    .atZone(ZoneId.systemDefault()) // 시스템 기본 시간대로 변환
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            news.setPubDate(isoDate);
        }

    }

    @Override
    public boolean isSupported(String rssUrl) {
        return rssUrl.contains("investing.com");
    }

    public String crawlContent(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(5000)
                    .get();

            // 1차: 최신 구조
            Element content = doc.selectFirst("div.WYSIWYG");

            // 2차 fallback: 구버전
            if (content == null) {
                content = doc.selectFirst("div.articlePage");
            }

            if (content == null) {
                return "";
            }

            // 불필요 태그 제거
            content.select("script, style, iframe, ads, .advertisement").remove();

            return content.text();

        } catch (IOException e) {
            return "";
        }
    }


    public static String parseContent(String url) {
        Document doc = null;
        try {
                 doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(5000)
                .get();
        } catch (IOException e) {
            return "";
        }

        Element article = doc.selectFirst("#article");
        if (article == null) return "";

        Elements paragraphs = article.select("p");

        List<String> cleanTexts = new ArrayList<>();

        for (Element p : paragraphs) {

            String text = p.text().trim();

            // 1. 빈 값 제거
            if (text.isEmpty()) continue;

            // 2. 광고/불필요 문장 필터링
            if (isNoise(text)) continue;

            cleanTexts.add(text);
        }

        // 3. 하나의 본문으로 합치기
        return cleanTexts.stream()
                .collect(Collectors.joining("\n\n"));
    }

    private static boolean isNoise(String text) {
        return text.contains("earnings calendar")
                || text.contains("InvestingPro")
                || text.contains("here")
                || text.contains("Stay up-to-date");
    }

}
