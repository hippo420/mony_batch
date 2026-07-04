package app.monybatch.mony.common.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DartHtmlToJsonParser {

    public static String parseXmlFile(String htmlContent) throws IOException
    {
        Document doc = Jsoup.parse(htmlContent);

        // 1. 실적 데이터 테이블 타겟팅
        //    위치(index)로 잡으면 정정공시처럼 표 개수/순서가 다른 양식에서 깨진다.
        //    (예: 정정공시는 '정정 비교표'가 앞에 끼어들어 실적표가 뒤로 밀린다)
        //    DART 양식의 실적표는 id 에 'RepeatTable' 이 포함되므로 이를 기준으로 선택한다.
        //    정정공시는 정정 비교표(D8)와 실제 실적표(D1)에 RepeatTable 이 둘 다 있는데,
        //    실제 실적표가 항상 마지막에 위치하므로 마지막 것을 사용한다.
        Elements repeatTables = doc.select("table[id*=RepeatTable]");
        if (repeatTables.isEmpty()) return"";
        Element targetTable = repeatTables.last();

        Map<String, List<String>> parsedResult = new LinkedHashMap<>();

        // 상위 계층 키워드를 기억하기 위한 상태 변수
        String parentRowKey = "";

        // [수정 포인트] 위아래로 나뉜 섹션 제목을 기억할 상태 변수 추가
        String pendingSectionTitle = "";

        // 2. 오직 <tr>과 <td> 태그의 DOM 구조만 순회
        for (Element row : targetTable.select("tr")) {
            Elements cells = row.select("th, td");
            if (cells.size() == 0) continue;

            Element firstCell = cells.first();
            String firstCellText = firstCell.text().trim();

            // =========================================================
            // [추가된 로직] 한 줄을 통째로 차지하는 서술형 데이터 처리
            // =========================================================
            // colspan이 크게 걸려있고, 셀이 1개뿐인 경우 (예: "4. 기타 투자판단...")
            if (cells.size() == 1 && firstCell.hasAttr("colspan") && Integer.parseInt(firstCell.attr("colspan")) >= 3) {
                Element inputSpan = firstCell.selectFirst("span.xforms_input");

                if (inputSpan == null) {
                    // 입력 폼이 없으면 -> 섹션 제목으로 판단하여 임시 기억
                    pendingSectionTitle = firstCellText;
                } else {
                    // 입력 폼이 있으면 -> 기억해둔 제목의 실제 내용(Value)으로 판단
                    if (!pendingSectionTitle.isEmpty()) {
                        String contentValue = inputSpan.text().trim();
                        // 빈 값이나 '-' 표기 필터링
                        if (!contentValue.isEmpty() && !contentValue.equals("-")) {
                            List<String> values = new ArrayList<>();
                            values.add(contentValue);
                            parsedResult.put(pendingSectionTitle, values);
                        }
                        pendingSectionTitle = ""; // 사용 후 초기화
                    }
                }
                continue; // 서술형 행 처리가 끝났으므로 다음 줄(tr)로 넘어감
            }
            // =========================================================

            if (cells.size() < 2) continue; // 데이터가 될 수 없는 단일 칸 행은 스킵

            // 구조 분류 변수
            String finalKey = "";
            int dataStartIndex = 1; // 기본적으로 두 번째 칸부터 데이터로 간주

            // [규칙 A] rowspan 속성이 있는 태그: 새로운 대분류의 시작
            if (firstCell.hasAttr("rowspan")) {
                parentRowKey = firstCellText; // 대분류 기억

                // rowspan이 있는 행은 그 다음 칸(index 1)이 소분류 키워드 역할을 함
                Element secondCell = cells.get(1);
                finalKey = parentRowKey + "_" + secondCell.text().trim();

                dataStartIndex = 2; // 세 번째 칸부터 실제 데이터
            }
            // [규칙 B] colspan 속성이 2 이상인 태그: 단일 와이드 항목 (예: 수주)
            else if (firstCell.hasAttr("colspan") && Integer.parseInt(firstCell.attr("colspan")) >= 2) {
                parentRowKey = ""; // 독립 항목이므로 대분류 초기화
                finalKey = firstCellText;

                dataStartIndex = 1; // 병합된 칸 다음부터 데이터
            }
            // [규칙 C] rowspan의 영향을 받는 하위 <td> 태그 (예: 누계실적)
            else if (!parentRowKey.isEmpty()) {
                finalKey = parentRowKey + "_" + firstCellText;

                dataStartIndex = 1; // 두 번째 칸부터 데이터
            }

            // [규칙 D] Key가 확정되었다면, 지정된 시작 인덱스부터 값 추출
            if (!finalKey.isEmpty()) {
                List<String> values = extractDataByClassOnly(cells, dataStartIndex);

                // 추출된 값이 하나라도 존재할 때만 결과 맵에 저장
                if (!values.isEmpty()) {
                    parsedResult.put(finalKey, values);
                }
            }
        }



        // 2. Jackson ObjectMapper 객체 생성
        ObjectMapper objectMapper = new ObjectMapper();
        String result ="";
        try {
            // 3. Map 객체를 바로 JSON 문자열로 변환 (Pretty Print 적용)
            // writerWithDefaultPrettyPrinter()를 빼고 writeValueAsString()만 쓰시면 줄바꿈 없는 한 줄 JSON이 됩니다.
            result= objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedResult);


        } catch (Exception e) {
            log.error("JSON 변환 중 오류가 발생했습니다: {}" , e.getMessage());
        }
        return result;
    }


    private static List<String> extractDataByClassOnly(Elements cells, int startIndex) {
        List<String> extractedValues = new ArrayList<>();

        for (int i = startIndex; i < cells.size(); i++) {
            Element currentCell = cells.get(i);

            // 오직 태그의 class 속성만을 기준으로 데이터 여부 판별
            Element inputSpan = currentCell.selectFirst("span.xforms_input");

            if (inputSpan != null) {
                String val = inputSpan.text().trim().replace(",", "");
                // 빈 값 필터링
                if (!val.isEmpty()) {
                    extractedValues.add(val);
                }
            }
        }
        return extractedValues;
    }
}
