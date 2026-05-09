package app.monybatch.mony.batch.dart.reader;

import app.monybatch.mony.batch.dart.cache.CorpCodeRegistry;
import app.monybatch.mony.batch.dart.dto.CorpMappingDto;
import app.monybatch.mony.batch.dart.dto.DartRssDto;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DartRssItemReader implements ItemReader<DartRssDto> {

    private final String rssUrl;
    private List<DartRssDto> items;
    private int nextIndex;

    @Autowired
    private CorpCodeRegistry registry;

    public DartRssItemReader(String rssUrl) {
        this.rssUrl = rssUrl;
        this.nextIndex = 0;
    }

    @Override
    public DartRssDto read() throws Exception {
        // 첫 호출 시에만 RSS를 파싱하여 리스트를 초기화
        if (items == null) {
            items = fetchRssData();
        }

        if (nextIndex < items.size()) {
            return items.get(nextIndex++);
        }

        return null; // 데이터가 더 이상 없으면 null 반환 (배치 종료)
    }

    private List<DartRssDto> fetchRssData() throws Exception {
        List<DartRssDto> resultList = new ArrayList<>();

        // XML 파싱 준비
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new URL(rssUrl).openStream());
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("item");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            DartRssDto dto = new DartRssDto();

            // 1. Title 파싱: (유가)삼성전자 - 특수관계인에대한출자
            String fullTitle = getTagValue("title", element);
            if (fullTitle != null && fullTitle.contains(" - ")) {
                // "(유가)" 제거 및 분리
                String cleanedTitle = fullTitle.replace("(유가)", "").replace("(코스닥)", "").trim();
                String[] parts = cleanedTitle.split(" - ");
                if (parts.length >= 2) {
                    String corpNm = parts[0].trim();

                    CorpMappingDto corp = registry.getCorpInfo(corpNm);
                    if(corp != null)
                    {
                        dto.setStockCode(corp.getStockCode());
                        dto.setCorpCode(corp.getCorpCode());
                    }
                    dto.setCorpNm(corpNm);
                    dto.setReportNm(parts[1].trim());
                }
            }

            // 2. Link & rcpNo(rceptNo) 파싱
            String link = getTagValue("link", element);
            dto.setLink(link);
            if (link != null && link.contains("rcpNo=")) {
                String rcpNo = link.substring(link.lastIndexOf("rcpNo=") + 6);
                dto.setRceptNo(rcpNo);
            }

            // 3. pubDate 파싱
            String pubDateStr = getTagValue("pubDate", element);
            if (pubDateStr != null) {
                dto.setPubDate(dateFormat.parse(pubDateStr));
            }

            // 4. dc:creator -> firNm 파싱
            dto.setFirNm(getTagValue("dc:creator", element));

            resultList.add(dto);
        }

        return resultList;
    }

    private String getTagValue(String tagName, Element element) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
}
