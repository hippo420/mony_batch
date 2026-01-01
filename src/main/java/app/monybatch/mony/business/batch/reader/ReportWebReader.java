package app.monybatch.mony.business.batch.reader;

import app.monybatch.mony.business.constant.ReportConst;
import app.monybatch.mony.business.entity.report.ReportDto;
import app.monybatch.mony.system.utils.DateUtil;
import app.monybatch.mony.system.utils.parser.NaverStockParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.batch.item.ItemReader;

import java.util.ArrayList;
import java.util.List;

import static app.monybatch.mony.system.utils.parser.NaverStockParser.getTotalPage;

@Slf4j
public class ReportWebReader implements ItemReader<List<ReportDto>> {
    private final String targetDate; // yyyy-MM-dd 형식
    private boolean isRead = false;

    public ReportWebReader(String targetDate) {
        // 하이픈이 없는 경우(20250810)를 대비해 포맷팅 로직을 넣는 것이 좋습니다.
        this.targetDate = DateUtil.getFormatDate(targetDate);
    }


    @Override
    public List<ReportDto> read() throws Exception {
        if (isRead) {
            return null; // 중요: 이미 읽었다면 null을 반환해야 Step이 종료됨
        }

        int curPage = 1;
        // sdate와 edate를 동일하게 설정하여 특정 날짜만 조회
        String url = String.format(
                ReportConst.NAVER_REPORT_URL,
                targetDate, targetDate,curPage
        );

        log.info("특정 날짜 리포트 조회 중: {}", url);

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();

        int totalPage = getTotalPage(doc);
        NaverStockParser parser = new NaverStockParser();
        List<ReportDto> reports = new ArrayList<>();
        for(int i = curPage; i <= totalPage;i++)
        {
            url = String.format(
                    ReportConst.NAVER_REPORT_URL,
                    targetDate, targetDate,i
            );

            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            reports.addAll(parser.parseHtml(doc.toString(),this.targetDate));

        }

        for(ReportDto rpt: reports)
        {
            doc = Jsoup.connect(rpt.getUrl())
                    .userAgent("Mozilla/5.0")
                    .get();
            parser.parseHtmlDetail(doc.toString(),rpt);
        }
        log.info("리포트 {}건 완료",reports.size());

        isRead = true;
        return reports;
    }
}
