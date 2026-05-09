package app.monybatch.mony.system.service;

import app.monybatch.mony.batch.dart.cache.CorpCodeRegistry;
import app.monybatch.mony.batch.dart.dto.CorpMappingDto;
import app.monybatch.mony.batch.dart.dto.DartRssDto;
import app.monybatch.mony.common.constant.AppConst;
import app.monybatch.mony.common.core.utils.DateUtil;
import app.monybatch.mony.domian.dart.DartBasicEntity;
import app.monybatch.mony.domian.dart.repository.DartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class DartRssSyncService {

    private final RedisTemplate<String, String> redisTemplate;
    private final CorpCodeRegistry registry;
    private final DartRepository repository;

    // Redis 키 유효기간 (공시는 보통 당일 중복 처리가 중요하므로 24시간 설정)
    private static final String REDIS_KEY_PREFIX = "sync:dart:rcp:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    /**
     * RSS 데이터를 가져와서 새로운 공시만 필터링하여 처리
     */
    public void syncRssData(String rssUrl) {
        try {
            List<DartRssDto> items = fetchRssData(rssUrl);

            for (DartRssDto dto : items) {
                String redisKey = REDIS_KEY_PREFIX;
                CorpMappingDto corp = registry.getCorpInfo(dto.getCorpNm());
                if(corp != null)
                    redisKey = redisKey.concat(corp.getCorpNm()+"_"+corp.getStockCode()+"_"+dto.getRceptNo()) ;
                else
                    redisKey = redisKey.concat("undefinded_undefinded_"+dto.getRceptNo()) ;

                // 1. Redis에 존재하지 않을 때만 실행 (Atomic 연산)
                Boolean isNew = redisTemplate.opsForValue()
                        .setIfAbsent(redisKey, "processed", CACHE_TTL);

                if (Boolean.TRUE.equals(isNew)) {
                    log.info("새로운 공시 발견: [{}] {}", dto.getCorpNm(), dto.getReportNm());

                    // 2. 후속 로직 처리 (DB 저장, 알림 발송, API 추가 호출 등)
                    processNewAnnouncement(dto);
                }
            }
        } catch (Exception e) {
            log.error("RSS 동기화 중 오류 발생", e);
        }
    }

    private List<DartRssDto> fetchRssData(String rssUrl) throws Exception {
        List<DartRssDto> resultList = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        URL url = URI.create(rssUrl).toURL();
        Document doc = builder.parse(url.openStream());
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("item");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            DartRssDto dto = parseElementToDto(element, dateFormat);
            if (dto != null) resultList.add(dto);
        }
        return resultList;
    }

    private DartRssDto parseElementToDto(Element element, SimpleDateFormat dateFormat) throws Exception {
        DartRssDto dto = new DartRssDto();

        // Title 파싱 및 CorpCodeRegistry 매핑 (기존 로직 유지)
        String fullTitle = getTagValue("title", element);
        if (fullTitle != null && fullTitle.contains(" - ")) {
            int idx = fullTitle.indexOf(" - ");
            if(fullTitle.contains("(") &&fullTitle.contains(")") && (idx > fullTitle.indexOf(")")) )
            {
                if(fullTitle.contains("(기타)")) return null;
                fullTitle = fullTitle.substring(fullTitle.indexOf(")")+1);
            }

            String cleanedTitle = fullTitle.trim();
            String[] parts = cleanedTitle.split(" - ");
            if (parts.length >= 2) {
                String corpNm = parts[0].trim();
                CorpMappingDto corp = registry.getCorpInfo(corpNm);
                if (corp != null) {
                    dto.setStockCode(corp.getStockCode());
                    dto.setCorpCode(corp.getCorpCode());
                }
                dto.setCorpNm(corpNm);
                dto.setReportNm(parts[1].trim());
            }
        }

        String link = getTagValue("link", element);
        dto.setLink(link);
        if (link != null && link.contains("rcpNo=")) {
            dto.setRceptNo(link.substring(link.lastIndexOf("rcpNo=") + 6));
        }

        String pubDateStr = getTagValue("pubDate", element);
        if (pubDateStr != null) dto.setPubDate(dateFormat.parse(pubDateStr));
        dto.setFirNm(getTagValue("dc:creator", element));
        //log.info(dto.toString());
        return dto;
    }

    private void processNewAnnouncement(DartRssDto dto) {
        DartBasicEntity entity = new DartBasicEntity();
        entity.setRCEPT_NO(dto.getRceptNo());
        String ymd = DateUtil.format(dto.getPubDate(),DateUtil.YYYYMMDD);
        entity.setRCEPT_DT(ymd);
        entity.setSTOCK_CODE(dto.getStockCode());
        entity.setREPORT_NM(dto.getReportNm());
        entity.setCORP_NAME(dto.getCorpNm());
        entity.setCORP_CODE(dto.getCorpCode());
        entity.setFIR_NM(dto.getFirNm());
        entity.setRM("");
        entity.setCORP_CLS(AppConst.CON_Y);
        entity.setPROC_YN(AppConst.CON_N);
        repository.save(entity);
    }

    private String getTagValue(String tagName, Element element) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        return (nodeList != null && nodeList.getLength() > 0) ? nodeList.item(0).getTextContent() : null;
    }
}
