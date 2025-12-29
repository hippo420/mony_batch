package app.monybatch.mony.business.batch.reader;

import app.monybatch.mony.business.constant.ReportConst;
import app.monybatch.mony.business.entity.report.ReportDto;
import app.monybatch.mony.system.utils.DateUtil;
import app.monybatch.mony.system.utils.parser.NaverStockParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.batch.item.ItemReader;

import java.util.List;

@Slf4j
public class ReportWebReader implements ItemReader<List<ReportDto>> {
    private final String targetDate; // yyyy-MM-dd 형식

    public ReportWebReader(String targetDate) {
        // 하이픈이 없는 경우(20250810)를 대비해 포맷팅 로직을 넣는 것이 좋습니다.
        this.targetDate = DateUtil.getFormatDate(targetDate);
    }


    @Override
    public List<ReportDto> read() throws Exception {


        // sdate와 edate를 동일하게 설정하여 특정 날짜만 조회
        String url = String.format(
                ReportConst.NAVER_REPORT_URL,
                targetDate, targetDate
        );

        log.info("특정 날짜 리포트 조회 중: {}", url);

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();
        NaverStockParser parser = new NaverStockParser();
        List<ReportDto> reports = parser.parseHtml(doc.toString(),this.targetDate);
        for(ReportDto rpt: reports)
        {
            doc = Jsoup.connect(rpt.getUrl())
                    .userAgent("Mozilla/5.0")
                    .get();
            parser.parseHtmlDetail(doc.toString(),rpt);
        }

        return reports;
    }
}
