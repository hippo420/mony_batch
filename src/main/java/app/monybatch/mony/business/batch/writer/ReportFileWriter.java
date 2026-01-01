package app.monybatch.mony.business.batch.writer;

import app.monybatch.mony.business.batch.service.GeminiApiClient;
import app.monybatch.mony.business.document.ReportIndex;
import app.monybatch.mony.business.entity.report.Keyword;
import app.monybatch.mony.business.entity.report.Report;
import app.monybatch.mony.business.entity.report.ReportDto;
import app.monybatch.mony.business.repository.es.ReportEsRepository;
import app.monybatch.mony.business.repository.jpa.ReportRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class ReportFileWriter implements ItemWriter<List<ReportDto>> {

    private final MinioUtil minioUtil;
    private final ReportRepository reportRepository;
    private final ReportEsRepository reportEsRepository;
    private final GeminiApiClient geminiApiClient;
    private final String savePath = "D:\\reports\\";

    @Override
    public void write(Chunk<? extends List<ReportDto>> chunks) throws Exception {

        Files.createDirectories(Paths.get(savePath));


        for (List<ReportDto> reportList : chunks) {
            List<Report> reports = new ArrayList<>();
            for (ReportDto report : reportList) {
                String safeName = report.getPdfFilename();
                if(!safeName.isEmpty())
                {
                    Path targetPath = Paths.get(savePath + safeName);

                    try (InputStream in = new URL(report.getPdfUrl()).openStream()) {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        //log.info("리포트 저장 완료: {}", safeName);
                    } catch (Exception e) {
                        log.error("다운로드 실패: {} - {}", report.getPdfFilename(), e.getMessage());
                    }
                    String extractedText = report.getContent();

                    //log.info("PDF 추출내용: {}",extractedText);

                    minioUtil.uploadFile(targetPath.toFile());

                    Files.delete(targetPath);
                    //log.info("리포트 삭제 완료: {}", targetPath);
                }
//                log.info("--- 추출 결과 ---");
//                log.info("발행일자: {}", report.getPubymd());
//                log.info("증권사: {}", report.getCompany());
//                log.info("종목코드: {}", report.getItem());
//                log.info("종목명: {}", report.getItemName());
//                log.info("제목 : {}", report.getTitle());
//                log.info("내용 : {}", report.getContent());
//                log.info("투자의견: {}", report.getInvest());
//                log.info("목표주가: {}", report.getTargetPrice());
//                log.info("상세URL: {}", report.getUrl());
//                log.info("PDF URL: {}", report.getPdfUrl());
//                log.info("PDF 파일명: {}", report.getPdfFilename());
                Report rpt = new Report();
                rpt.setBasymd(report.getPubymd());
                rpt.setItem(report.getItem());
                rpt.setItemName(report.getItemName());
                rpt.setInvest(report.getInvest());
                rpt.setCompany(report.getCompany());
                rpt.setContent(report.getContent());
                rpt.setPrice(report.getTargetPrice());
                rpt.setPdfUrl(report.getPdfUrl());
                rpt.setPdfFilename(report.getPdfFilename());
                reports.add(rpt);

            }
            reportRepository.saveAll(reports);
            List<ReportIndex> documents = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < reports.size(); i++) {
                sb.append(reports.get(i).getItemName());
                sb.append(":");
                sb.append(reports.get(i).getContent());
                if(i != reports.size()-1)
                    sb.append("|");
            }
            Map<String, List<Keyword>> keywords = geminiApiClient.extractKeywords(sb.toString());
            for (int i = 0; i < reports.size(); i++)
            {

                ReportIndex indexData = ReportIndex.builder()
                        .id(reports.get(i).getId().toString()) // RDB ID와 동기화
                        .author(reports.get(i).getInvest())    // 엔티티 필드에 따라 조정
                        .company(reports.get(i).getCompany())
                        .content(reports.get(i).getContent())
                        .keywords(keywords.get(reports.get(i).getItem()))   // 집계용 (List<String>)
                        .reportDate(reports.get(i).getBasymd())
                        .minioUrl(reports.get(i).getPdfUrl())
                        .itemCode(reports.get(i).getItem())
                        .itemName(reports.get(i).getItemName())
                        .build();
                documents.add(indexData);
            }
            reportEsRepository.saveAll(documents);
        }
        log.info("PDF 파일 처리 : {}건 완료",chunks.size());
    }
}
