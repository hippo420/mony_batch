package app.monybatch.mony.system.utils.parser;

import app.monybatch.mony.business.constant.ReportConst;
import app.monybatch.mony.business.entity.report.Invest;
import app.monybatch.mony.business.entity.report.ReportDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class NaverStockParser implements ParserUtil<ReportDto>{
    private final String UNDER_SCORE = "_";

    @Override
    public List<ReportDto>  parseHtml(String data,String date) {
        List<ReportDto> datas = new ArrayList<>();
        Document doc = Jsoup.parse(data);

        // a.stock_item을 기준으로 반복
        Elements stockLinks = doc.select("a.stock_item");

        for (Element link : stockLinks) {
            // 현재 행(tr)을 먼저 가져옵니다. (매우 중요)
            Element row = link.closest("tr");
            if (row == null) continue;

            String href = link.attr("href"); // /item/main.naver?code=213420
            String stockName = link.text();  // 덕산네오룩스

            // 1. 상세 페이지 URL 추출 (두 번째 td의 a 태그)
            String detailUrl = row.select("td").get(1).select("a").attr("href");

            // 2. 종목코드 파싱
            String stockCode = "";
            if (href.contains("code=")) {
                stockCode = href.split("code=")[1];
            }

            // 3. PDF URL 추출 (수정된 부분)
            // link가 아닌 row(tr)에서 td.file 클래스 내부의 a 태그를 찾습니다.
            String pdfUrl = "";
            String pdfFileName ="";
            Element fileLink = row.selectFirst("td.file a");
            if (fileLink != null) {
                pdfUrl = fileLink.attr("href");
                //pdfFileName = createPdfFileName(date,);
            }

            // 4. 네이버 상세 주소 조립
            String url = String.format(
                    ReportConst.NAVER_REPORT_DETAIL_URL,
                    detailUrl, date, date
            );

            datas.add(new ReportDto(date.replaceAll("-",""),stockCode, stockName, url, pdfUrl,pdfFileName));
        }
        return datas;
    }

    private String createPdfFileName(String date, String company, String item,String itemName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(date);
        sb.append(UNDER_SCORE);
        sb.append(company);
        sb.append(UNDER_SCORE);
        sb.append(item);
        sb.append("(");
        sb.append(itemName);
        sb.append(").pdf");
        return sb.toString();
    }

    @Override
    public void parseHtmlDetail(String data, ReportDto report) {

        Document doc = Jsoup.parse(data);
        // --- 1번 추출: 종목명, 제목, 증권사 ---
        Element header = doc.selectFirst("th.view_sbj");
        if (header != null) {
            // em 태그(종목명) 추출 후 제거하여 제목만 남기기
            Element stockEm = header.selectFirst("span em");
            if (stockEm != null) {
                report.setItemName(stockEm.text());
                stockEm.remove(); // 제목 추출을 위해 em 태그 제거
            }

            // source 클래스(증권사 정보) 추출 후 제거
            Element sourceP = header.selectFirst("p.source");
            if (sourceP != null) {
                String sourceText = sourceP.text(); // "교보증권|2025.12.26..."
                report.setCompany(sourceText.split("\\|")[0].trim());
                sourceP.remove();
            }

            // 남은 텍스트가 리포트 제목
            report.setTitle(header.text().trim());
        }

        // --- 2번 추출: 목표가, 투자의견 ---
        Element infoBox = doc.selectFirst("div.view_info_1");
        if (infoBox != null) {
            // 목표가 숫자만 추출
            Element priceEm = infoBox.selectFirst("em.money");

            if (priceEm != null)
            {
                String price = priceEm.text();
                log.info("price: [{}]",price);
                if(!price.isBlank() && !price.equals("없음"))
                    report.setTargetPrice(new BigDecimal(price.replaceAll(",", "")));
                else
                    report.setTargetPrice(BigDecimal.ZERO);
            }
            // 투자의견 추출
            Element opinionEm = infoBox.selectFirst("em.coment");
            if (opinionEm != null) report.setInvest(Invest.findCode(opinionEm.text()));
        }

        // --- 3번 추출: 본문 내용 ---
        Element contentDiv = doc.selectFirst("td.view_cnt div");
        if (contentDiv != null) {
            // p 태그들의 텍스트를 모두 합치기
            StringBuilder sb = new StringBuilder();
            for (Element p : contentDiv.select("p")) {
                String text = p.text().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
            report.setContent(sb.toString().trim());
        }
        if(!report.getPdfUrl().isEmpty())
        {
            String filename = createPdfFileName(report.getPubymd(),report.getCompany(),report.getItem(),report.getItemName());
            report.setPdfFilename(filename);
        }

    }


}
