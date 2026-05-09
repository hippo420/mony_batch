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
            item.setIndustCode(temp.getIndustCode());
        });
        return item;
    }
}
