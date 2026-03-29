package app.monybatch.mony.batch.news.parser;

import app.monybatch.mony.domian.news.entity.NewsDto;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class HanGeongNewsParser implements NewsParser {
    @Override
    public void parseSpecific(SyndEntry entry, NewsDto news) {
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
    }

    @Override
    public boolean isSupported(String rssUrl) {
        return rssUrl.contains("wowtv.co.kr");
    }
}