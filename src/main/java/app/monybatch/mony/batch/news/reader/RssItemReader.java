package app.monybatch.mony.batch.news.reader;

import app.monybatch.mony.batch.news.parser.NewsParser;
import app.monybatch.mony.batch.news.parser.NewsParserFactory;
import app.monybatch.mony.domian.news.entity.NewsDto;
import app.monybatch.mony.domian.news.entity.NewsRss;
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
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class RssItemReader implements ItemReader<NewsDto> {

    private final NewsRss rss;
    private Iterator<SyndEntry> itemIterator;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private final NewsParserFactory parserFactory;

    public RssItemReader(NewsRss rss, NewsParserFactory parserFactory) {
        this.rss = rss;
        this.parserFactory=parserFactory;

    }

    @Override
    public NewsDto read() throws Exception {
        if (itemIterator == null) {
            // URL 연결 및 스트림 열기
            URLConnection connection = new URL(rss.getLink()).openConnection();
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
                log.info("Reading RSS feed from: {}, Found {} articles.", rss.getCompany(), entries.size());
            } catch (Exception e) {
                log.error("Failed to read or parse RSS feed: {}",  rss.getLink(), e);
                // 오류 발생 시 빈 리스트로 초기화하여 다음 Reader로 넘어가도록 함
                this.itemIterator = Collections.emptyIterator();
            }
        }

        if (itemIterator.hasNext()) {
            SyndEntry entry = itemIterator.next();
            return parseEntry(entry,rss.getCompany(),rss.getCategory());
        } else {
            return null; // End of feed
        }
    }

    private NewsDto parseEntry(SyndEntry entry,String company, String category) {
        NewsDto news = new NewsDto();
        news.setCompany(company);
        news.setCategory(category);
        String rssUrl = rss.getLink();


        // 2. 언론사별 특화 파싱 (다형성 활용)
        NewsParser parser = parserFactory.getParser(rssUrl);
        parser.parseSpecific(entry, news);



        return news;
    }


}
