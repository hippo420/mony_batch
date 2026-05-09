package app.monybatch.mony.batch.stock.processor;

import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.entity.StockTemp;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class StockTempProcessor implements ItemProcessor<Stock, StockTemp> {

    @Override
    public StockTemp process(Stock item) {
        StockTemp temp = new StockTemp();
        temp.setIsuCd(item.getIsuCd());
        temp.setISU_SRT_CD(item.getISU_SRT_CD());
        temp.setISU_NM(item.getISU_NM());
        temp.setISU_ABBRV(item.getISU_ABBRV());
        temp.setISU_ENG_NM(item.getISU_ENG_NM());
        temp.setLIST_DD(item.getLIST_DD());
        temp.setMKT_TP_NM(item.getMKT_TP_NM());
        temp.setSECUGRP_NM(item.getSECUGRP_NM());
        temp.setSECT_TP_NM(item.getSECT_TP_NM());
        temp.setKIND_STKCERT_TP_NM(item.getKIND_STKCERT_TP_NM());
        temp.setPARVAL(item.getPARVAL());
        temp.setLIST_SHRS(item.getLIST_SHRS());
        // DART 필드 보존
        temp.setCORP_CODE(item.getCORP_CODE());
        temp.setCEO_NM(item.getCEO_NM());
        temp.setBIZR_NO(item.getBIZR_NO());
        temp.setADRES(item.getADRES());
        temp.setHM_URL(item.getHM_URL());
        temp.setIR_URL(item.getIR_URL());
        temp.setPHN_NO(item.getPHN_NO());
        temp.setFAX_NO(item.getFAX_NO());
        temp.setINDUTY_CODE(item.getINDUTY_CODE());
        temp.setEST_DT(item.getEST_DT());
        temp.setACC_MT(item.getACC_MT());
        temp.setIndustCode(item.getIndustCode());
        return temp;
    }
}
