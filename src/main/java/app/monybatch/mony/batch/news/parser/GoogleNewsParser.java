package app.monybatch.mony.batch.news.parser;

import app.monybatch.mony.domian.news.entity.NewsDto;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class GoogleNewsParser implements NewsParser {

    @Override
    public void parseSpecific(SyndEntry entry, NewsDto news) {
        // 1. Description 처리 (HTML 태그 제거 및 순수 텍스트 추출)
        // Google News는 <a>태그와 <font>태그가 섞여 나옵니다.
        if(entry.getTitle()!= null){
            String title = entry.getTitle();
            title = title.substring(0,title.lastIndexOf("-")-1);
            news.setTitle(cleanGoogleHtml(title));
        }

        if(entry.getLink()!= null){
            String link = entry.getLink();
            news.setOriginallink(cleanGoogleHtml(link));
            news.setLink(cleanGoogleHtml(link));
        }

        if (entry.getDescription() != null) {
            String rawDescription = entry.getDescription().getValue();
            news.setDescription(cleanGoogleHtml(rawDescription));
        }

        // 2. 소스(언론사) 정보 추출
        // Rome 라이브러리에서 <source> 태그는 getSource()를 통해 접근하거나
        // ForeignMarkup에서 가져올 수 있습니다.
        if (entry.getSource() != null) {
            news.setCompany(entry.getSource().getTitle());
        } else {
            // 소스 태그가 없을 경우 타이틀 마지막의 " - 언론사명" 패턴에서 추출 시도
            extractCompanyFromTitle(entry.getTitle(), news);
        }

        Date pubDate = entry.getPublishedDate();

        if (pubDate != null) {
            // Date 객체를 시스템 기본 시간대(한국)의 OffsetDateTime으로 변환
            String formattedDate = pubDate.toInstant()
                    .atZone(ZoneId.of("Asia/Seoul")) // 한국 시간대 명시
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            news.setPubDate(formattedDate);
        } else {
            // 날짜 정보가 없는 경우 현재 시간으로 세팅 (선택 사항)
            news.setPubDate(ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
    }

    @Override
    public boolean isSupported(String rssUrl) {
        return rssUrl.contains("news.google.com");
    }

    /**
     * Google News 특유의 HTML 구조에서 텍스트만 추출
     */
    private String cleanGoogleHtml(String html) {
        if (html == null) return "";
        // 모든 HTML 태그를 제거하고, &nbsp; 등을 일반 공백으로 변환
        return html.replaceAll("<[^>]*>", "")
                .replaceAll("&nbsp;", " ")
                .trim();
    }

    /**
     * 타이틀 뒤에 붙는 " - KBS 뉴스" 형태에서 언론사 분리
     */
    private void extractCompanyFromTitle(String title, NewsDto news) {
        if (title != null && title.contains(" - ")) {
            int lastIndex = title.lastIndexOf(" - ");
            String realTitle = title.substring(0, lastIndex).trim();
            String company = title.substring(lastIndex + 3).trim();

            news.setTitle(realTitle); // 원본 타이틀에서 언론사명 제거 후 재설정
            news.setCompany(company);
        }
    }
}
