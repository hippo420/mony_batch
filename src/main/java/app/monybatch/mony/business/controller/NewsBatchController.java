package app.monybatch.mony.business.controller;


import app.monybatch.mony.business.service.NewsBatchService;
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
public class NewsBatchController {

    private final NewsBatchService service;

    @GetMapping("/news/fetchNews")
    public void fetchItem(@RequestParam(name="basDd") String basDd) {
        log.info("뉴스 크롤링 배치처리");
        service.fetchNews(basDd,false);

    }

    @GetMapping("/news/fetchNewsForced")
    public void fetchItemForced(@RequestParam(name="basDd") String basDd) {
        log.info("[강제] 뉴스 크롤링 배치처리");
        service.fetchNews(basDd,true);

    }

}
