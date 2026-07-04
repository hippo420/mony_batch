package app.monybatch.mony.batch.stock.processor;

import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.repository.StockTempRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockProcessor implements ItemProcessor<Stock, Stock> {

    private final StockTempRepository stockTempRepository;

    @Override
    public Stock process(Stock item) {
        // StockTemp(기존 DB 스냅샷)에서 DART 필드 보존
        stockTempRepository.findById(item.getIsuCd()).ifPresent(temp -> {
            item.setCORP_CODE(temp.getCORP_CODE());
            item.setCEO_NM(temp.getCEO_NM());
            item.setBIZR_NO(temp.getBIZR_NO());
            item.setADRES(temp.getADRES());
            item.setHM_URL(temp.getHM_URL());
            item.setIR_URL(temp.getIR_URL());
            item.setPHN_NO(temp.getPHN_NO());
            item.setFAX_NO(temp.getFAX_NO());
            item.setINDUTY_CODE(temp.getINDUTY_CODE());
            item.setEST_DT(temp.getEST_DT());
            item.setACC_MT(temp.getACC_MT());
            // industry_code(FK)는 별도 쿼리로 수동 관리하는 값이므로 배치에서 보존
            item.setIndustryCode(temp.getIndustryCode());
        });

        // 관리종목(소속부없음)이면 상장폐지 대상(Y), 벗어나면 N으로 복원
        if ("관리종목(소속부없음)".equals(item.getSECT_TP_NM())) {
            item.setDELIST_YN("Y");
        } else {
            item.setDELIST_YN("N");
        }
        return item;
    }
}
