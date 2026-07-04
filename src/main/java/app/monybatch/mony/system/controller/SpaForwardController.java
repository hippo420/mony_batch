package app.monybatch.mony.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * vue-batch-admin SPA 라우트 새로고침 대응.
 * 정적 리소스로 서빙되는 화면에서 /jobs 등의 경로를 직접 열어도 index.html로 포워딩한다.
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/jobs", "/executions", "/schedules"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
