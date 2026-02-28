package app.monybatch.mony.business.controller;

import app.monybatch.mony.business.batch.service.OllamaModelClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final OllamaModelClient client;

    @PostMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String  streamChat(@RequestBody String prompt)
    {
        log.info("prompt ={}",prompt);
        String res = client.generate(prompt);
        res = res==null ? "답변없음": res;
        log.info("답변 :{}",res);
        return res;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String  chat(@RequestBody String prompt)
    {
        log.info("prompt ={}",prompt);
        String res = client.chat(prompt);
        res = res==null ? "답변없음": res;
        log.info("답변 :{}",res);
        return res;
    }
}
