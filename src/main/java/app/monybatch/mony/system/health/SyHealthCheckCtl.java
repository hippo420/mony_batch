package app.monybatch.mony.system.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController("/system")
public class SyHealthCheckCtl {
    @GetMapping("/health")
    public String heathCheck() {
        log.info("Health check started");
        return "OK";
    }
}
