package app.monybatch.mony.batch.stock.processor;

import app.monybatch.mony.domian.stock.dto.InvestorTradeRaw;
import app.monybatch.mony.domian.trade.entity.Trade;
import app.monybatch.mony.domian.trade.repository.TradeRepository;

import java.util.HashMap;
import java.util.Map;

public class PileTradeProcessor {

    private final TradeRepository tradeRepository;
    private final Map<String, String> maxDateCache = new HashMap<>();

    public PileTradeProcessor(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    private String getMaxDate(String isuSrtCd) {
        if (!maxDateCache.containsKey(isuSrtCd)) {
            maxDateCache.put(isuSrtCd, tradeRepository.findMaxDateByIsuSrtCd(isuSrtCd));
        }
        return maxDateCache.get(isuSrtCd);
    }

    public Trade processInternal(InvestorTradeRaw raw) {
        String isuSrtCd = raw.getIsuSrtCd();
        String stckBsopDate = raw.getStckBsopDate();

        if (isuSrtCd == null || stckBsopDate == null || stckBsopDate.isBlank()) return null;

        String maxDate = getMaxDate(isuSrtCd);
        if (maxDate != null && stckBsopDate.compareTo(maxDate) <= 0) {
            return null;
        }

        Trade trade = new Trade();
        trade.setStckBsopDate(stckBsopDate);
        trade.setIsuSrtCd(isuSrtCd);

        trade.setStckClpr(raw.getStckClpr());
        trade.setPrdyVrss(raw.getPrdyVrss());
        trade.setPrdyVrssSign(raw.getPrdyVrssSign());
        trade.setPrdyCtrt(raw.getPrdyCtrt());
        trade.setAcmlVol(raw.getAcmlVol());
        trade.setAcmlTrPbmn(raw.getAcmlTrPbmn());
        trade.setStckOprc(raw.getStckOprc());
        trade.setStckHgpr(raw.getStckHgpr());
        trade.setStckLwpr(raw.getStckLwpr());

        trade.setFrgnNtbyQty(raw.getFrgnNtbyQty());
        trade.setFrgnRegNtbyQty(raw.getFrgnRegNtbyQty());
        trade.setFrgnNregNtbyQty(raw.getFrgnNregNtbyQty());
        trade.setPrsnNtbyQty(raw.getPrsnNtbyQty());
        trade.setOrgnNtbyQty(raw.getOrgnNtbyQty());
        trade.setScrtNtbyQty(raw.getScrtNtbyQty());
        trade.setIvtrNtbyQty(raw.getIvtrNtbyQty());
        trade.setPeFundNtbyVol(raw.getPeFundNtbyVol());
        trade.setBankNtbyQty(raw.getBankNtbyQty());
        trade.setInsuNtbyQty(raw.getInsuNtbyQty());
        trade.setMrbnNtbyQty(raw.getMrbnNtbyQty());
        trade.setFundNtbyQty(raw.getFundNtbyQty());
        trade.setEtcNtbyQty(raw.getEtcNtbyQty());
        trade.setEtcCorpNtbyVol(raw.getEtcCorpNtbyVol());
        trade.setEtcOrgtNtbyVol(raw.getEtcOrgtNtbyVol());

        trade.setFrgnRegNtbyPbmn(raw.getFrgnRegNtbyPbmn());
        trade.setFrgnNtbyTrPbmn(raw.getFrgnNtbyTrPbmn());
        trade.setFrgnNregNtbyPbmn(raw.getFrgnNregNtbyPbmn());
        trade.setPrsnNtbyTrPbmn(raw.getPrsnNtbyTrPbmn());
        trade.setOrgnNtbyTrPbmn(raw.getOrgnNtbyTrPbmn());
        trade.setScrtNtbyTrPbmn(raw.getScrtNtbyTrPbmn());
        trade.setPeFundNtbyTrPbmn(raw.getPeFundNtbyTrPbmn());
        trade.setIvtrNtbyTrPbmn(raw.getIvtrNtbyTrPbmn());
        trade.setBankNtbyTrPbmn(raw.getBankNtbyTrPbmn());
        trade.setInsuNtbyTrPbmn(raw.getInsuNtbyTrPbmn());
        trade.setMrbnNtbyTrPbmn(raw.getMrbnNtbyTrPbmn());
        trade.setFundNtbyTrPbmn(raw.getFundNtbyTrPbmn());
        trade.setEtcNtbyTrPbmn(raw.getEtcNtbyTrPbmn());
        trade.setEtcCorpNtbyTrPbmn(raw.getEtcCorpNtbyTrPbmn());
        trade.setEtcOrgtNtbyTrPbmn(raw.getEtcOrgtNtbyTrPbmn());

        trade.setFrgnSelnVol(raw.getFrgnSelnVol());
        trade.setFrgnShnuVol(raw.getFrgnShnuVol());
        trade.setFrgnSelnTrPbmn(raw.getFrgnSelnTrPbmn());
        trade.setFrgnShnuTrPbmn(raw.getFrgnShnuTrPbmn());
        trade.setFrgnRegAskpQty(raw.getFrgnRegAskpQty());
        trade.setFrgnRegBidpQty(raw.getFrgnRegBidpQty());
        trade.setFrgnRegAskpPbmn(raw.getFrgnRegAskpPbmn());
        trade.setFrgnRegBidpPbmn(raw.getFrgnRegBidpPbmn());
        trade.setFrgnNregAskpQty(raw.getFrgnNregAskpQty());
        trade.setFrgnNregBidpQty(raw.getFrgnNregBidpQty());
        trade.setFrgnNregAskpPbmn(raw.getFrgnNregAskpPbmn());
        trade.setFrgnNregBidpPbmn(raw.getFrgnNregBidpPbmn());

        trade.setPrsnSelnVol(raw.getPrsnSelnVol());
        trade.setPrsnShnuVol(raw.getPrsnShnuVol());
        trade.setPrsnSelnTrPbmn(raw.getPrsnSelnTrPbmn());
        trade.setPrsnShnuTrPbmn(raw.getPrsnShnuTrPbmn());

        trade.setOrgnSelnVol(raw.getOrgnSelnVol());
        trade.setOrgnShnuVol(raw.getOrgnShnuVol());
        trade.setOrgnSelnTrPbmn(raw.getOrgnSelnTrPbmn());
        trade.setOrgnShnuTrPbmn(raw.getOrgnShnuTrPbmn());

        trade.setScrtSelnVol(raw.getScrtSelnVol());
        trade.setScrtShnuVol(raw.getScrtShnuVol());
        trade.setScrtSelnTrPbmn(raw.getScrtSelnTrPbmn());
        trade.setScrtShnuTrPbmn(raw.getScrtShnuTrPbmn());

        trade.setIvtrSelnVol(raw.getIvtrSelnVol());
        trade.setIvtrShnuVol(raw.getIvtrShnuVol());
        trade.setIvtrSelnTrPbmn(raw.getIvtrSelnTrPbmn());
        trade.setIvtrShnuTrPbmn(raw.getIvtrShnuTrPbmn());

        trade.setPeFundSelnVol(raw.getPeFundSelnVol());
        trade.setPeFundShnuVol(raw.getPeFundShnuVol());
        trade.setPeFundSelnTrPbmn(raw.getPeFundSelnTrPbmn());
        trade.setPeFundShnuTrPbmn(raw.getPeFundShnuTrPbmn());

        trade.setBankSelnVol(raw.getBankSelnVol());
        trade.setBankShnuVol(raw.getBankShnuVol());
        trade.setBankSelnTrPbmn(raw.getBankSelnTrPbmn());
        trade.setBankShnuTrPbmn(raw.getBankShnuTrPbmn());

        trade.setInsuSelnVol(raw.getInsuSelnVol());
        trade.setInsuShnuVol(raw.getInsuShnuVol());
        trade.setInsuSelnTrPbmn(raw.getInsuSelnTrPbmn());
        trade.setInsuShnuTrPbmn(raw.getInsuShnuTrPbmn());

        trade.setMrbnSelnVol(raw.getMrbnSelnVol());
        trade.setMrbnShnuVol(raw.getMrbnShnuVol());
        trade.setMrbnSelnTrPbmn(raw.getMrbnSelnTrPbmn());
        trade.setMrbnShnuTrPbmn(raw.getMrbnShnuTrPbmn());

        trade.setFundSelnVol(raw.getFundSelnVol());
        trade.setFundShnuVol(raw.getFundShnuVol());
        trade.setFundSelnTrPbmn(raw.getFundSelnTrPbmn());
        trade.setFundShnuTrPbmn(raw.getFundShnuTrPbmn());

        trade.setEtcSelnVol(raw.getEtcSelnVol());
        trade.setEtcShnuVol(raw.getEtcShnuVol());
        trade.setEtcSelnTrPbmn(raw.getEtcSelnTrPbmn());
        trade.setEtcShnuTrPbmn(raw.getEtcShnuTrPbmn());

        trade.setEtcOrgtSelnVol(raw.getEtcOrgtSelnVol());
        trade.setEtcOrgtShnuVol(raw.getEtcOrgtShnuVol());
        trade.setEtcOrgtSelnTrPbmn(raw.getEtcOrgtSelnTrPbmn());
        trade.setEtcOrgtShnuTrPbmn(raw.getEtcOrgtShnuTrPbmn());

        trade.setEtcCorpSelnVol(raw.getEtcCorpSelnVol());
        trade.setEtcCorpShnuVol(raw.getEtcCorpShnuVol());
        trade.setEtcCorpSelnTrPbmn(raw.getEtcCorpSelnTrPbmn());
        trade.setEtcCorpShnuTrPbmn(raw.getEtcCorpShnuTrPbmn());

        trade.setBoldYn(raw.getBoldYn());

        return trade;
    }
}
