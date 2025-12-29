package app.monybatch.mony.business.batch.writer;

import app.monybatch.mony.business.batch.service.GeminiApiClient;
import app.monybatch.mony.business.entity.report.ReportDto;
import app.monybatch.mony.system.utils.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Slf4j
public class ReportFileWriter implements ItemWriter<List<ReportDto>> {


    private final MinioUtil minioUtil;
    private final GeminiApiClient geminiApiClient;
    private final String savePath = "D:\\reports\\";

    @Override
    public void write(Chunk<? extends List<ReportDto>> chunks) throws Exception {

        Files.createDirectories(Paths.get(savePath));


        for (List<ReportDto> reportList : chunks) {
            for (ReportDto report : reportList) {
                String safeName = report.getPdfFilename();
                if(!safeName.isEmpty())
                {
                    Path targetPath = Paths.get(savePath + safeName);

                    try (InputStream in = new URL(report.getPdfUrl()).openStream()) {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        log.info("리포트 저장 완료: {}", safeName);
                    } catch (Exception e) {
                        log.error("다운로드 실패: {} - {}", report.getPdfFilename(), e.getMessage());
                    }
                    String extractedText = report.getContent();

                    log.info("PDF 추출내용: {}",extractedText);

                    minioUtil.uploadFile(targetPath.toFile());

                    Files.delete(targetPath);
                    log.info("리포트 삭제 완료: {}", targetPath);
                }
            }
            //String report = geminiApiClient.requestReportSummary(reports);
        }
        log.info("PDF 파일 처리 : {}건 완료",chunks.size());

    }

    public Map<String, String> extractFinancialInfo(String content) {
        Map<String, String> result = new HashMap<>();

        // 1. 목표가 추출 (숫자와 '원' 또는 'TP' 단어 조합 찾기)
        Pattern pricePattern = Pattern.compile("(목표주가|TP|Target Price)\\s*[:]*\\s*([0-9,]+)\\s*원");
        Matcher priceMatcher = pricePattern.matcher(content);
        if (priceMatcher.find()) {
            result.put("targetPrice", priceMatcher.group(2).replace(",", ""));
        }


        // 2. 투자의견 추출 (Buy, Hold, Strong Buy 등)
        Pattern opinionPattern = Pattern.compile("(투자의견|Rating)\\s*[:]*\\s*(Buy|Hold|Sell|매수|보유|매도)", Pattern.CASE_INSENSITIVE);
        Matcher opinionMatcher = opinionPattern.matcher(content);
        if (opinionMatcher.find()) {
            result.put("rating", opinionMatcher.group(2).toUpperCase());
        }

        log.info("목표가 : {}",result.get("targetPrice"));
        log.info("투자의견 : {}",result.get("rating"));

        return result;
    }
}
