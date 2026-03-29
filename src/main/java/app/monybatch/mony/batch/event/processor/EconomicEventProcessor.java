package app.monybatch.mony.batch.event.processor;

import app.monybatch.mony.batch.event.parser.EconomicCategoryClassifier;
import app.monybatch.mony.batch.event.parser.EconomicImportanceClassifier;
import app.monybatch.mony.common.core.utils.HashUtil;
import app.monybatch.mony.domian.event.dto.EconomicEventDto;
import app.monybatch.mony.domian.event.dto.EventCategory;
import app.monybatch.mony.domian.event.entity.EconomicEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class EconomicEventProcessor implements ItemProcessor<EconomicEventDto, EconomicEvent> {


    @Override
    public EconomicEvent process(EconomicEventDto item) throws Exception {

        if(EventCategory.UNKNOWN.equals(EconomicCategoryClassifier.classify(item.getEvent())))
            return null;

        return EconomicEvent.builder()
                .id(HashUtil.generateMD5Hash(item.getDate()+item.getTime()+item.getEvent()))
                .eventDate(convertDate(item.getDate()))
                .eventTime(convertTimeTo24H(item.getTime()))
                .country(item.getCountry())
                .event(item.getEvent())
                .actual(item.getActual())
                .previous(item.getPrevious())
                .forecast(item.getForecast())
                .consensus(item.getConsensus())
                .category(EconomicCategoryClassifier.classify(item.getEvent()))
                .impact(EconomicImportanceClassifier.classify(item.getEvent(),EconomicCategoryClassifier.classify(item.getEvent()).getDescription()))
                .build();
    }

    public String convertDate (String date)
    {
        String res ="";
        date = date.trim();
        if(date.contains("/"))
        {
            String[] spl = date.split("/");
            res = res.concat(spl[2].trim());
            res = res.concat(spl[1].trim());
            res = res.concat(spl[0].trim());
        }
        else{
            //TODO
            log.error("convertDate error");
        }
        return res;

    }

    public String convertTimeTo24H(String time) {
        // 1. 공백 제거 및 대문자 변환 (비교를 위해)
        time = time.trim().toUpperCase();

        // 2. AM/PM 표시와 시간 숫자 부분 분리 (예: "02:30 PM" -> ["02:30", "PM"])
        String[] parts = time.split(" ");
        String hhmm = parts[0];
        String ampm = parts[1];

        // 3. 시(hour)와 분(min) 분리
        String[] hhmmParts = hhmm.split(":");
        int hour = Integer.parseInt(hhmmParts[0]);
        String minute = hhmmParts[1];

        // 4. 변환 로직 적용
        if (ampm.equals("AM")) {
            // 오전 12시는 00시로 변환
            if (hour == 12) {
                hour = 0;
            }
        } else if (ampm.equals("PM")) {
            // 오후 12시가 아니면 12를 더함 (오후 1시 -> 13시)
            if (hour != 12) {
                hour += 12;
            }
        }

        // 5. 결과 반환 (두 자리 숫자로 포맷팅)
        return String.format("%02d%s", hour, minute);
    }



}
