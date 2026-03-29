package app.monybatch.mony.batch.news.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsParserFactory {
    private final List<NewsParser> parsers;
    private final DefaultNewsParser defaultParser; // 빈으로 주입받음

    public NewsParser getParser(String rssUrl) {
        return parsers.stream()
                .filter(p -> p.isSupported(rssUrl))
                .findFirst()
                .orElse(defaultParser); // 매칭되는 언론사가 없으면 기본 파서 반환
    }
}
