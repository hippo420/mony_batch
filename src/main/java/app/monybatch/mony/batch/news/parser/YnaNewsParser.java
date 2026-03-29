package app.monybatch.mony.batch.news.parser;

import app.monybatch.mony.domian.news.entity.NewsDto;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.stereotype.Component;

@Component
public class YnaNewsParser implements NewsParser {
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

    }

    @Override
    public boolean isSupported(String rssUrl) {
        return rssUrl.contains("yna.co.kr");
    }
}
