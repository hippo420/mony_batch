package app.monybatch.mony.batch.event.writer;

import app.monybatch.mony.domian.event.dto.EconomicEventDto;
import app.monybatch.mony.domian.event.entity.EconomicEvent;
import app.monybatch.mony.domian.event.repository.EconomicEventRepository;
import app.monybatch.mony.infra.llm.OllamaModelClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class EconomicEventItemWriter implements ItemWriter<EconomicEvent> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OllamaModelClient ollamaModelClient;

    @Autowired
    private EconomicEventRepository eventRepository;

    @Override
    public void write(Chunk<? extends EconomicEvent> chunk) throws Exception {
        // 1. Chunk에서 원본 엔티티 리스트 추출
//        @SuppressWarnings("unchecked")
//        List<EconomicEvent> origin = (List<EconomicEvent>) chunk.getItems();
//
//        // 2. 필터링 및 가공 로직 수행 (기존에 만드신 메서드 호출)
//        // temp에는 업데이트할 데이터(category, impact 등)가 담겨 있어야 함
//        List<EconomicEventDto> temp = filterEvent(origin);
//
//        // 3. 성능 최적화를 위해 temp를 Map으로 변환 (ID 기준)
//        Map<String, EconomicEventDto> tempMap = temp.stream()
//                .filter(dto -> dto.getId() != null)
//                .collect(Collectors.toMap(EconomicEventDto::getId, dto -> dto));
//
//        // 4. origin 엔티티의 값을 temp 데이터로 업데이트
//        for (EconomicEvent entity : origin) {
//            EconomicEventDto sourceDto = tempMap.get(entity.getId());
//
//            if (sourceDto != null) {
//                // 엔티티 내부의 값을 변경 (JPA Dirty Checking에 의해 자동 반영됨)
//                if(sourceDto.getCategory()!=null)
//                    entity.updateMetadata(sourceDto.getCategory().getDescription());
//            }
//            log.info("Batch Write: {}", entity);
//        }

        // 5. (선택 사항) 명시적 저장
        // 사실 JPA 영속 상태라면 필드 변경만으로 충분하지만,
        // 로직의 명확성을 위해 호출해도 무방합니다.
        eventRepository.saveAll(chunk);

        log.info("Batch Write 완료: {}건 업데이트", chunk.size());
    }

    private List<EconomicEventDto> filterEvent(List<EconomicEvent> items) throws Exception{
        // 3. 분할 처리를 위한 프롬프트 구성
        StringBuilder newsBuilder = new StringBuilder();
        long start = System.currentTimeMillis();

        for (EconomicEvent item : items) {
            newsBuilder.append(item.getId()).append("-").append(item.getEvent()).append("\n");
        }

        String prompt = String.format("""
                            역할: 당신은 금융 뉴스 기반 투자 분석 시스템이다.

                            입력:
                            - 이벤트 리스트 (각 이벤트는 id(HASH)-이벤트명)

                            목표:
                            각 이벤트마다 아래를 추출한다:
                            1. 카테코리 키워드 (Monetary, Employment, Activity, Inflation, Energy)
                               - MONETARY(통화정책) : Fed 연설(연준의장), 국채 매입 등
                               - EMPLOYMENT(고용) : JOLT 채용공고, ADP 고용변화, ISM 고용지수 등
                               - ACTIVITY(실물경제) : ISM 제조업 PMI, 시카고 PMI, 소매 판매 등
                               - INFLATION(물가/주택) : S&P/케이스실러 주택지수, ISM 제조 가격 등
                               - ENERGY(에너지/농산물) : EIA 원유재고, 곡물 재고(옥수수, 대두 등)
                               
                               판단 기준:
                                    [1] 통화정책 (MONETARY)
                                    - 금리 인상/인하 시점 예측. 달러 가치와 채권 금리에 직결됨.
                    
                                    [2] 고용 (EMPLOYMENT)
                                    - 경제의 기초 체력. 고용이 좋으면 금리 인하 가능성이 낮아짐 (매파적).
                    
                                    [3] 실물 경제 (ACTIVITY)
                                    - 경제 성장률(GDP)의 선행 지표. 주식 시장의 펀더멘탈 판단 기준.
                    
                                    [4] 물가/주택 (INFLATION)
                                    - 인플레이션 압력 측정. 연준의 금리 결정에 가장 큰 영향을 미침.
                    
                                    [5] 에너지/농산물 (ENERGY)
                                    - 원자재 가격 결정. 에너지 및 식품 관련 기업 주가에 직접 영향.

                            출력 형식(JSON):
                            [
                              {
                                "id":"",
                                "category": "MONETARY or EMPLOYMENT or ACTIVITY or INFLATION or ENERGY"
                              },
                              {
                                "id":"",
                                "category": "MONETARY or EMPLOYMENT or ACTIVITY or INFLATION or ENERGY" 
                              }
                            ]
                    
                            규칙:
                            1. 이벤트 개수와 동일한 개수 출력
                            2. 입력 순서 유지
                            3. 추측 금지 (근거 없는 의견 금지)
                            4. JSON형태로만 출력할 것
                            
                            이벤트 내용
                            %s
            """, newsBuilder);

        String data = ollamaModelClient.generate(prompt);

        long end = System.currentTimeMillis();
        log.info("{}건 처리 시간 : {}",items.size(),end-start);
        return objectMapper.readValue(
                data,
                new TypeReference<List<EconomicEventDto>>() {}
        );
    }
}
