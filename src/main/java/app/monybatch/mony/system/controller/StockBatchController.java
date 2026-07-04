package app.monybatch.mony.system.controller;


import app.monybatch.mony.common.core.utils.DateUtil;
import app.monybatch.mony.system.service.StockBatchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/batch")
public class StockBatchController {

    private final StockBatchService service;

    @GetMapping("/stock/fetchItem")
    public void fetchItem(@RequestParam(name="basDd") String basDd) {
        log.info("유가증권 목록 Fetch 컨트롤러");
        service.fetchItem(basDd,false);

    }

    @GetMapping("/stock/fetchItemForced")
    public void fetchItemForced(@RequestParam(name="basDd") String basDd) {
        log.info("유가증권 목록 Fetch 컨트롤러");
        service.fetchItem(basDd,true);

    }
    //@Scheduled("* * ")
    @GetMapping("/stock/fetchPrice")
    public void fetchPrice(@RequestParam(name="basDd") String basDd) {
        log.info("유가증권 종가 Fetch 컨트롤러 - 일자:[{}]",basDd);
        service.fetchPrice(basDd,false);

    }

    @GetMapping("/stock/fetchPriceForced")
    public void fetchPriceForced(@RequestParam(name="basDd") String basDd) {
        log.info("유가증권 종가 Fetch 컨트롤러(강제) - 일자:[{}]",basDd);
        service.fetchPrice(basDd,true);

    }

    @GetMapping("/stock/mapping")
    public void mapDart() {
        log.info("DART-KRX 종목매핑");
        service.fetchMappring(true);

    }

    @GetMapping("/stock/fetchDartInfoAll")
    public void fetchDartInfoAll(@RequestParam(name="basDd") String basDd) {
        log.info("공시정보 Fetch 컨트롤러 - 일자:[{}] ~ [{}]",basDd);
        List<String> targetDate = DateUtil.getDatesUntilToday(basDd);
        for(String day : targetDate)
            service.fetchDartInfoAll(day,true);

    }

    @GetMapping("/stock/fetchDartInfo")
    public void fetchDartInfo(@RequestParam(name="fromYmd", required = false) String fromYmd, @RequestParam(name="toYmd", required = false) String toYmd) {
        log.info("공시정보 Fetch 컨트롤러 - 일자:[{}] ~ [{}]",fromYmd,toYmd);
        service.fetchDartInfo(fromYmd,toYmd,false);

    }

    @GetMapping("/stock/fetchDartInfoForced")
    public void fetchDartInfoForced(@RequestParam(name="fromYmd", required = false) String fromYmd, @RequestParam(name="toYmd", required = false) String toYmd) {
        log.info("공시정보 Fetch 컨트롤러(강제) - 일자:[{}]~ [{}]",fromYmd,toYmd);
        service.fetchDartInfo(fromYmd,toYmd,true);

    }

    @GetMapping("/stock/fetchDartAcct")
    public void fetchDartAcct(@RequestParam(name="corp_code") String corp_code,@RequestParam(name="bsns_year") String bsns_year) {
        log.info("공시정보- 재무정보 Fetch 컨트롤러 - 고유번호:[{}], 사업년도 : [{}]",corp_code,bsns_year);
        service.fetchDartAcct(corp_code,bsns_year,false);

    }

    @GetMapping("/stock/fetchDartAcctForced")
    public void fetchDartAcctForced(@RequestParam(name="corp_code") String corp_code,@RequestParam(name="bsns_year") String bsns_year) {
        log.info("공시정보- 재무정보 Fetch 컨트롤러(강제) - 고유번호:[{}], 사업년도 : [{}]",corp_code,bsns_year);
        service.fetchDartAcct(corp_code,bsns_year,true);

    }

    @GetMapping("/stock/fetchInvestorTrade")
    public void fetchInvestorTrade(@RequestParam(name="basDd") String basDd) {
        log.info("투자매매동향 - Fetch 컨트롤러 - 일자:[{}]",basDd);
        service.fetchInvestorTrade(basDd,false);

    }

    @GetMapping("/stock/fetchInvestorTradeForced")
    public void fetchInvestorTradeForced(@RequestParam(name="basDd") String basDd) {
        log.info("투자매매동향 - Fetch 컨트롤러(강제) - 일자:[{}]",basDd);
        service.fetchInvestorTrade(basDd,true);

    }

    @GetMapping("/stock/fetchPileTrade")
    public void fetchPileTrade(@RequestParam(name="basDd") String basDd) {
        log.info("종목별 투자자 매매동향 - Fetch 컨트롤러 - 일자:[{}]",basDd);
        service.fetchPileTrade(basDd,false);
    }

    @GetMapping("/stock/fetchPileTradeForced")
    public void fetchPileTradeForced(@RequestParam(name="basDd") String basDd) {
        log.info("종목별 투자자 매매동향 - Fetch 컨트롤러(강제) - 일자:[{}]",basDd);
        service.fetchPileTrade(basDd,true);
    }
}
