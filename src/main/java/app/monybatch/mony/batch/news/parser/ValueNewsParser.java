package app.monybatch.mony.batch.news.parser;

import app.monybatch.mony.domian.news.entity.NewsDto;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.stereotype.Component;

@Component
public class ValueNewsParser implements NewsParser {
    @Override
    public void parseSpecific(SyndEntry entry, NewsDto news) {
        // 구글 뉴스만의 특화 로직
        if (entry.getDescription() != null) {
            news.setDescription(entry.getDescription().getValue());
        }
    }

    @Override
    public boolean isSupported(String rssUrl) {
        return rssUrl.contains("thevaluenews.co.kr");
    }
}