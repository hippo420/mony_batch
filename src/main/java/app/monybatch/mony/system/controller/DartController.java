package app.monybatch.mony.system.controller;


import app.monybatch.mony.system.service.DartBatchService;
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
public class DartController {

    private final DartBatchService service;

    @GetMapping("/dart/fetchItem")
    public void fetchItem(@RequestParam(name="basDd") String basDd) {
        log.info("DART 공시 Fetch 컨트롤러");
        service.fetchItem(basDd,false);

    }

    @GetMapping("/dart/fetchItemForced")
    public void fetchItemForced(@RequestParam(name="basDd") String basDd) {
        log.info("DART 공시 Fetch 컨트롤러");
        service.fetchItem(basDd,true);

    }

}
