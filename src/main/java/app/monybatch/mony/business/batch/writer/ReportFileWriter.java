package app.monybatch.mony.business.batch.writer;

import app.monybatch.mony.business.batch.service.GeminiApiClient;
import app.monybatch.mony.business.document.ReportIndex;
import app.monybatch.mony.business.entity.report.Keyword;
import app.monybatch.mony.business.entity.report.Report;
import app.monybatch.mony.business.entity.report.ReportDto;
import app.monybatch.mony.business.repository.es.ReportEsRepository;
import app.monybatch.mony.business.repository.jpa.ReportRepository;
import app.monybatch.mony.system.utils.DateUtil;
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

import static app.monybatch.mony.business.constant.ReportConst.MINIO_URL_REPORTS;

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
                    minioUtil.uploadFile(targetPath.toFile());

                    Files.delete(targetPath);
                    //log.info("리포트 삭제 완료: {}", targetPath);
                }
                Report rpt = new Report();
                rpt.setBasymd(report.getPubymd());
                rpt.setItem(report.getItem());
                rpt.setItemName(report.getItemName());
                rpt.setInvest(report.getInvest());
                rpt.setTitle(report.getTitle());
                rpt.setCompany(report.getCompany());
                rpt.setContent(report.getContent());
                rpt.setPrice(report.getTargetPrice());
                rpt.setPdfUrl(String.format(MINIO_URL_REPORTS,report.getPdfFilename()));
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
                log.info("키워드추출: {}",reports.get(i).getItemName());
                print(keywords.get(reports.get(i).getItemName()));
                ReportIndex indexData = ReportIndex.builder()
                        .id(reports.get(i).getId().toString()) // RDB ID와 동기화
                        .author("")    // 엔티티 필드에 따라 조정
                        .company(reports.get(i).getCompany())
                        .content(reports.get(i).getContent())
                        .keywords(keywords.get(reports.get(i).getItemName()))   // 집계용 (List<String>)
                        .reportDate(DateUtil.getFormatDate(reports.get(i).getBasymd()))
                        .minioUrl(String.format(MINIO_URL_REPORTS,reports.get(i).getItemName()))
                        .itemCode(reports.get(i).getItem())
                        .itemName(reports.get(i).getItemName())
                        .title(reports.get(i).getTitle())
                        .build();
                documents.add(indexData);
            }
            reportEsRepository.saveAll(documents);
        }
        //log.info("PDF 파일 처리 : {}건 완료",chunks.size());
    }

    private void print(List<Keyword> key){
        for(Keyword k : key)
            log.info("키워드 : {}, 점수:{}",k.getKeyword(),k.getScore());
    }

}
