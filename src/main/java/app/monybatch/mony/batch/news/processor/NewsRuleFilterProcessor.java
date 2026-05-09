package app.monybatch.mony.batch.news.processor;

import app.monybatch.mony.domian.news.dto.FilterType;
import app.monybatch.mony.domian.news.entity.NewsDto;
import app.monybatch.mony.infra.llm.OllamaModelClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;
@Slf4j
public class NewsRuleFilterProcessor implements ItemProcessor<NewsDto, NewsDto> {

    @Autowired
    private OllamaModelClient ollamaModelClient;

    // 1. 블랙리스트 패턴 (하나라도 걸리면 제외)News
    private static final Pattern BLACKLIST_PATTERN = Pattern.compile(
            ".*(연예|드라마|예능|열애|결별|컴백|시청률|야구|축구|농구|올림픽|월드컵|프리미어리그|" +
                    "홈런|살인|폭행|성폭행|음주운전|화재|사망|부고|태풍|폭우|미세먼지|맛집|다이어트|" +
                    "공연|뮤지컬|여야|공천|당대표).*"
    );

    // 2. 화이트리스트 패턴 (하나라도 걸리면 무조건 통과)
    private static final Pattern WHITELIST_PATTERN = Pattern.compile(
            ".*(영업이익|매출액|흑자전환|어닝서프라이즈|무상증자|유상증자|배당|자사주 소각|"
            +"액면분할|상장폐지|거래정지|단일판매|공급계약|목표가|투자의견|IPO|공모가|블록딜|M&A|"
            +"당기순이익|적자전환|적자지속|어닝쇼크|잠정실적|컨센서스|가이던스|최대실적|자본잠식|감자|"
                    +"전환사채|CB|신주인수권부사채|BW|공개매수|인적분할|물적분할|경영권 분쟁|스톡옵션|"
                    +"배당락|자사주 매입|상한가|하한가|신고가|신저가|공매도|숏커버링|관리종목|투자경고|"
                    +"투자주의|외국인 순매수|기관 순매수|비중확대|비중축소|상향조정|하향조정|밸류업).*"
    );

    //추가: 경량 pre-filter (LLM 호출 줄이기)
    private static final Pattern LIGHT_FINANCE_PATTERN = Pattern.compile(
            "(주식|증시|코스피|코스닥|금리|환율|반도체|AI|배터리|전기차|기업|실적)"
    );

    @Override
    public NewsDto process(NewsDto item) throws Exception {
        String title = item.getTitle();
        if(title==null) return null;
        String content = item.getDescription();

        String total = title.concat(content);


        // [단계 1] 화이트리스트 검사 (가장 빠르고 확실한 놈부터 처리)
        // 화이트리스트에 걸리면 비싼 LLM을 호출하지 않고 바로 통과시킵니다.
        if (WHITELIST_PATTERN.matcher(total).find()) {
            item.setFilterType(FilterType.RULE_WHITELIST); // 나중에 통계용으로 기록해두면 좋습니다.
            log.debug("PASS[WHITE] {}", title);
            return item;
        }

        // [단계 2] 블랙리스트 검사
        // 쓸데없는 기사는 여기서 버립니다 (return null).
        if (BLACKLIST_PATTERN.matcher(total).find()) {
            log.debug("DROP[BLACK] {}", title);
            return null;
        }

        // 3. 경량 필터 → 금융 키워드 없으면 버림 (LLM 호출 방지 핵심)
//        if (!LIGHT_FINANCE_PATTERN.matcher(total).find()) {
//            log.debug("DROP[LIGHT] {}", title);
//            return null;
//        }

        String prompt = String.format("""
                            역할: 분류기
                
                            규칙:
                            - 반드시 '1' 또는 '0'만 출력
                            - 다른 텍스트 절대 금지
                            - 영어기사는 한국어로 번역할 것
                            
                            기준:
                            - 주식, 기업, 경제 관련 → 1
                            - 그 외 → 0
                            기사제목
                            [%s]
                            
                            기사내용
                            [%s]
            """, title,content);

        String result = ollamaModelClient.generate(prompt);

        if("1".equals(normalize(result)))
        {
            item.setFilterType(FilterType.LLM_PASSED);
            log.debug("PASS[LLM] {}", title);
            return item;
        }
        else{
            log.debug("DROP[LLM] {}", title);
            return null;
        }


    }

    private String normalize(String raw) {
        if (raw == null) return "0";

        raw = raw.trim();

        // "1입니다", "1.", "1\n" 등 처리
        if (raw.startsWith("1")) return "1";
        if (raw.startsWith("0")) return "0";

        return "0";
    }
}
