package app.monybatch.mony.batch.event.parser;

import app.monybatch.mony.domian.event.dto.EconomicEventDto;
import app.monybatch.mony.domian.event.dto.MarketImpact;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class EconomicCalendarParser {

    public static List<EconomicEventDto> parse(Document doc) {

        List<EconomicEventDto> result = new ArrayList<>();

        // 1. 불필요한 헤더 제거
        doc.select("thead.hidden-head").remove();

        String currentDate = null;

        // 테이블 내의 모든 thead와 tr을 순차적으로 탐색
        // tr[data-url]을 사용하여 데이터가 있는 행만 타겟팅
        Elements elements = doc.select("table#calendar thead, table#calendar tr[data-url]");

        for (Element el : elements) {

            // 2. 날짜 행 처리 (thead 에서 날짜 추출)
            if (el.tagName().equals("thead")) {
                Element dateEl = el.selectFirst("th");
                if (dateEl != null && !dateEl.text().contains("실제") && !dateEl.text().contains("Actual")) {
                    currentDate = dateEl.text().trim();
                }
                continue;
            }

            // 3. 이벤트 Row 처리
            // 중요: > td 를 사용하여 직계 자식 td만 가져옵니다. (중첩 테이블 내부 td 제외)
            Elements tds = el.select("> td");
            if (tds.size() < 3) continue;

            // [0] 시간 추출
            String time = tds.get(0).text().trim();
            
            // [1] 국가 추출
            Element countryCell = tds.get(1);
            String country = countryCell.select(".flag").attr("title");
            if (country == null || country.isEmpty()) {
                country = countryCell.select(".calendar-iso").text().trim();
            }

            // [2] 이벤트 명 추출
            Element eventCell = tds.get(2);
            String eventName = "";
            Element aTag = eventCell.selectFirst("a");
            Element spanTag = eventCell.selectFirst("span");
            
            if (aTag != null && !aTag.text().isEmpty()) {
                eventName = aTag.text().trim();
            } else if (spanTag != null && !spanTag.text().isEmpty()) {
                eventName = spanTag.text().trim();
            } else {
                eventName = eventCell.text().trim();
            }

            // [3~6] 값 추출 (ID 기반 우선 탐색)
            String actual = extractText(tds.get(3), "#actual");
            String previous = tds.size() > 4 ? extractText(tds.get(4), "#previous") : "";
            String consensus = tds.size() > 5 ? extractText(tds.get(5), "#consensus") : "";
            String forecast = tds.size() > 6 ? extractText(tds.get(6), "#forecast") : "";

            // 4. Impact 파싱
            MarketImpact impact = parseImpact(tds.get(2)); 

            EconomicEventDto dto = EconomicEventDto.builder()
                    .date(currentDate)
                    .time(time)
                    .country(country)
                    .event(eventName.replaceAll("파우웰", "파월"))
                    .actual(emptyToNull(actual))
                    .previous(emptyToNull(previous))
                    .consensus(emptyToNull(consensus))
                    .forecast(emptyToNull(forecast))
                    .impact(impact)
                    .build();

            result.add(dto);
        }

        return result;
    }

    private static String extractText(Element td, String selector) {
        Element target = td.selectFirst(selector);
        // ID 셀렉터로 못 찾으면 td의 직접적인 텍스트만 가져오기 시도
        return (target != null) ? target.text().trim() : td.ownText().trim();
    }

    private static MarketImpact parseImpact(Element td) {
        // 별(star) 개수 기반 판단
        int stars = td.select("i.glyphicon-star").size(); 
        if (stars == 0) return MarketImpact.LOW;

        return switch (stars) {
            case 3 -> MarketImpact.HIGH;
            case 2 -> MarketImpact.MEDIUM;
            default -> MarketImpact.LOW;
        };
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
