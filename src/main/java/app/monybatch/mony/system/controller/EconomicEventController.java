package app.monybatch.mony.system.controller;

import app.monybatch.mony.system.service.EconomicEventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/batch")
@RestController
@AllArgsConstructor
public class EconomicEventController {
    private final EconomicEventService service;

    @GetMapping("/event/fetchEvents")
    public void fetchEvents(@RequestParam(name="basDd") String basDd) {
        log.info("일정 배치처리");
        service.fetchEvents(basDd,false);

    }

    @GetMapping("/event/fetchEventsForced")
    public void fetchEventsForced(@RequestParam(name="basDd") String basDd) {
        log.info("[강제] 일정 배치처리");
        service.fetchEvents(basDd,true);

    }
}
