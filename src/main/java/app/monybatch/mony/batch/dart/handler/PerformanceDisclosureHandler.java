package app.monybatch.mony.batch.dart.handler;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.DartHtmlToJsonParser;
import app.monybatch.mony.common.core.utils.DateUtil;
import app.monybatch.mony.common.core.utils.OpenAPIUtil;
import app.monybatch.mony.domian.earning.dto.EarningsReleaseDto;
import app.monybatch.mony.domian.earning.dto.ReportQuarter;
import app.monybatch.mony.domian.earning.entity.EarningsRelease;
import app.monybatch.mony.domian.earning.repository.EarningsReleaseRepository;
import app.monybatch.mony.infra.llm.OllamaModelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 영업(잠정)실적 공시 처리 파이프라인.
 *
 * 일반 공시(JSON API → 엔티티 적재)와 달리, 실적 공시는 다음 흐름으로 처리한다.
 *   1) OpenDART /api/document.xml 호출 → 원본 문서 ZIP 다운로드 → 압축 해제 → 보고서 XML
 *   2) DartHtmlToJsonParser.parseXmlFile 로 실적 표를 JSON 으로 추출
 *   3) OllamaModelClient 로 실적 분석/요약
 *
 * DartDisclosureConsumer 가 DisclosureType.BUSINESS_PERFORMANCE 일 때 이 핸들러로 위임한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceDisclosureHandler {

    private final OllamaModelClient ollamaModelClient;
    private final EarningsReleaseRepository repository;

    public EarningsRelease handle(DartRssQueueDto item) {
        // 1. 원본 문서 다운로드 → ZIP 해제 → 보고서 XML
        String reportXml = OpenAPIUtil.requestDartDocument(item.getRceptNo());
        if (reportXml == null || reportXml.isBlank()) {
            log.warn("실적 공시 문서 없음: rceptNo={}", item.getRceptNo());
            return null;
        }

        // 2. 보고서 XML → 실적 표 JSON
        String parsedJson;
        try {
            parsedJson = DartHtmlToJsonParser.parseXmlFile(reportXml);
        } catch (Exception e) {
            log.error("실적 보고서 파싱 실패: rceptNo={}, error={}", item.getRceptNo(), e.getMessage());
            return null;
        }
        if (parsedJson == null || !parsedJson.contains(":")) { // 빈 결과({ }) 방어
            log.warn("실적 표 데이터 추출 결과 없음: rceptNo={}", item.getRceptNo());
            return null;
        }

        // 3. LLM 분석/요약
        //String summary = ollamaModelClient.summarizePerformanceDisclosure(item.getCorpNm(), item.getStockCode(), reportXml);

        String summary = ollamaModelClient.getEarningRelease(item.getCorpNm(), item.getStockCode(), parsedJson);

        log.info("실적 공시 분석 완료: rceptNo={}, corpNm={}, summary={}",
                item.getRceptNo(), item.getCorpNm(), summary);
        EarningsReleaseDto current = null;
        // 2. ObjectMapper를 통해 JSON 문자열을 Java 객체로 바로 매핑
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            current = objectMapper.readValue(summary, EarningsReleaseDto.class);
        }
        catch (Exception e)
        {
            log.error("JSON 변환오류 : {}",e.getMessage());
        }

        EarningsRelease preYear = repository.findByIsuSrtCdAndReportYearAndReportQuarter(item.getStockCode(), DateUtil.getCurrentYearByInt(-1), DateUtil.getCurrentReportQuarterByInt(0)).orElse(null);

        String qoqYear ="";
        ReportQuarter qoqReportQuarter = null;
        if(ReportQuarter.Q1.equals(DateUtil.getCurrentReportQuarter()))
        {
            qoqYear= DateUtil.getCurrentYearByInt(-1);
            qoqReportQuarter=ReportQuarter.Q4;
        }else
        {
            qoqYear= DateUtil.getCurrentYearByInt(0);
            qoqReportQuarter=DateUtil.getCurrentReportQuarterByInt(-1);
        }

        EarningsRelease preQuarter = repository.findByIsuSrtCdAndReportYearAndReportQuarter(item.getStockCode(), qoqYear, qoqReportQuarter).orElse(null);

        Double revenueYoy = calculatePercentageGrowth(current.getRevenue(), preYear.getRevenue());
        Double revenueQoq = calculatePercentageGrowth(current.getRevenue(), preQuarter.getRevenue());

        Double profitYoy = calculatePercentageGrowth(current.getOperatingProfit(), preYear.getOperatingProfit());
        Double profitQoq = calculatePercentageGrowth(current.getOperatingProfit(), preQuarter.getOperatingProfit());


        //return earningsRelease;
        return null;
    }

    /**
     * 두 BigDecimal 간의 증감률(%)을 계산하는 유틸리티 메서드
     * 공식: ((이번실적 - 직전실적) / 직전실적) * 100
     */
    private Double calculatePercentageGrowth(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null) {
            return null;
        }

        // 분모가 0이거나 0에 가까우면 계산 불가 (Null 혹은 0.0 처리)
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        // (Current - Previous)
        BigDecimal difference = current.subtract(previous);

        // (Difference / Previous) * 100
        // 소수점 6자리까지 계산 후 반올림하여 최종 Double 변환
        BigDecimal growthRate = difference
                .divide(previous, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return growthRate.doubleValue();
    }
}