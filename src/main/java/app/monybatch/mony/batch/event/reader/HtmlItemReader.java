package app.monybatch.mony.batch.event.reader;

import app.monybatch.mony.batch.event.parser.EconomicCalendarParser;
import app.monybatch.mony.domian.event.dto.EconomicEventDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HtmlItemReader extends AbstractItemCountingItemStreamItemReader<EconomicEventDto> {

    private final List<String> urls;
    private List<EconomicEventDto> items;

    public HtmlItemReader(List<String> urls) {
        setName(ClassUtils.getShortName(HtmlItemReader.class));
        this.urls = urls;
    }

    /**
     * Reader가 열릴 때 1회 실행됩니다. (초기화 및 데이터 로드)
     */
    @Override
    protected void doOpen() throws Exception {
        log.info("==== HTML 데이터 페칭 시작 ====");
        this.items = new ArrayList<>();
        for (String url : urls) {

            try {
                // 운영망에서는 Timeout 설정이 매우 중요합니다. (무한 대기 방지)
                Document doc = Jsoup.connect(url)
                        .timeout(10000) // 10초 타임아웃
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36") // 상세한 User-Agent
                        .header("Accept-Language", "en-US,en;q=0.9,ko;q=0.8") // 언어 헤더 추가 (사이트별 대응)
                        .get();


                items.addAll(EconomicCalendarParser.parse(doc));
                log.info("==== HTML 데이터 페칭 완료. 총 {}건 적재 ====", items.size());

            } catch (IOException e) {
                log.error("URL 접속 또는 HTML을 읽어오는 데 실패했습니다. URL: {}", url, e);
                throw e; // 접속 실패는 Batch Step 자체를 실패시키기 위해 throw
            }
        }

    }

    /**
     * Chunk 단위로 호출되며 데이터를 1건씩 반환합니다.
     */
    @Override
    protected EconomicEventDto doRead() throws Exception {
        if (items == null || items.isEmpty()) return null;

        // getCurrentItemCount()는 1부터 시작하므로 인덱스 접근 시 -1 처리
        int currentIndex = getCurrentItemCount() - 1;

        if (currentIndex < items.size()) {
            return items.get(currentIndex);
        } else {
            return null; // 모든 데이터를 다 읽었으면 null 반환
        }
    }

    /**
     * Reader가 닫힐 때 실행됩니다. (리소스 정리)
     */
    @Override
    protected void doClose() throws Exception {
        if (items != null) {
            items.clear(); // OOM(Out Of Memory) 방지를 위한 참조 해제
            items = null;
        }
        log.info("==== HtmlItemReader 리소스 정리 완료 ====");
    }
}
