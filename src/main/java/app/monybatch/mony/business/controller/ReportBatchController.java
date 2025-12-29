package app.monybatch.mony.business.controller;


import app.monybatch.mony.business.service.ReportBatchService;
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
public class ReportBatchController {

    private final ReportBatchService service;

    @GetMapping("/report/download")
    public void fetchItem(@RequestParam(name="basDd") String basDd) {
        log.info("보고서 다운 배치처리");
        service.downReport(basDd,false);

    }

    @GetMapping("/report/downloadForced")
    public void fetchItemForced(@RequestParam(name="basDd") String basDd) {
        log.info("[강제] 보고서 다운  배치처리");
        service.downReport(basDd,true);

    }

}
