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
            ".*(영업이익|매출액|흑자전환|어닝서프라이즈|무상증자|유상증자|배당|자사주 소각|" +
                    "액면분할|상장폐지|거래정지|단일판매|공급계약|목표가|투자의견|IPO|공모가|블록딜|M&A).*"
    );

    @Override
    public NewsDto process(NewsDto item) throws Exception {
        String title = item.getTitle();

        // [단계 1] 화이트리스트 검사 (가장 빠르고 확실한 놈부터 처리)
        // 화이트리스트에 걸리면 비싼 LLM을 호출하지 않고 바로 통과시킵니다.
        if (WHITELIST_PATTERN.matcher(title).matches()) {
            item.setFilterType(FilterType.RULE_WHITELIST); // 나중에 통계용으로 기록해두면 좋습니다.
            log.info("WHITELIST_PATTERN 통과 ->  기사:{} , 내용:{}",item.getTitle(),item.getDescription());
            return item;
        }

        // [단계 2] 블랙리스트 검사
        // 쓸데없는 기사는 여기서 버립니다 (return null).
        if (BLACKLIST_PATTERN.matcher(title).matches()) {
            log.info("BLACKLIST_PATTERN 제외 ->  기사:{} , 내용:{}",item.getTitle(),item.getDescription());
            return null;
        }

        String prompt = String.format("""
                            다음 뉴스 제목이 주식 투자나 기업 경제와 관련 있으면 '1', 아니면 '0'이라고만 답해.
                            
                            기사제목
                            [%s]
                            
                            기사내용
                            [%s]
            """, item.getTitle(),item.getDescription());

        String result = ollamaModelClient.generate(prompt);

        if("1".equals(result))
        {
            item.setFilterType(FilterType.LLM_PASSED);
            log.info("LLM 선정 -> 기사:{} , 내용:{}",item.getTitle(),item.getDescription());
            return item;
        }
        else{
            log.info("LLM 제외 ->  기사:{} , 내용:{}",item.getTitle(),item.getDescription());
            return null;
        }


    }
}
