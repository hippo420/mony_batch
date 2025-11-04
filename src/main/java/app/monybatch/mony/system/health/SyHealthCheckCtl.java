package app.monybatch.mony.system.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/system")
public class SyHealthCheckCtl {
    @GetMapping("/health")
    public String heathCheck() {
        return "OK";
    }
}
