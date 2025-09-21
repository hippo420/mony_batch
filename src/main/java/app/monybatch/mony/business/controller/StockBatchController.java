package app.monybatch.mony.business.controller;


import app.monybatch.mony.business.service.StockBatchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/batch")
public class StockBatchController {

    private final StockBatchService service;

    @GetMapping("/stock/fetchItem")
    public void fetchItem(@RequestParam(name="basDd") String basDd) {
        log.info("유가증권 목록 Fetch 컨트롤러");
        service.fetchItem(basDd);


    }

}
