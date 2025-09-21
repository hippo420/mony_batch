package app.monybatch.mony.system.utils;

import app.monybatch.mony.business.constant.StockConstant;
import app.monybatch.mony.system.core.constant.DataType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;



@Slf4j
public class OpenAPIUtil {


    public static JSONObject requestApi(String Url, String path, MultiValueMap<String,String> params, DataType resultType)  {


        WebClient webClient = WebClient.builder()
                .baseUrl(Url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        //log.info("===================OPEN_API START===================");

        String result =  webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParams(params)
                        .build())
                .headers(headers -> {
                    if (Url.contains("data-dbg.krx.co.kr")) {
                        headers.add("AUTH_KEY", StockConstant.KRX_API_KEY);
                    } else if (Url.contains("opendart.fss.or.kr")) {
                        headers.add("CRTFC_KEY", StockConstant.DART_KEY);
                    }
                })
                .retrieve()
                .bodyToFlux(String.class)
                .reduce(String::concat)
                .block();

        log.info("API Response: {}",result);

        if (result == null || result.isEmpty()) {
            log.error("Empty response from API");
            throw new RuntimeException("Empty response from API");
        }
        log.info(result);
        JSONObject jsonObject = null;

        try {
            if (resultType == DataType.DATA_XML) {
                // XML을 JSON으로 변환
                //jsonObject = XML.toJSONObject(result);
            } else {
                // JSON 파싱
                JSONParser parser = new JSONParser();
                jsonObject = (JSONObject) parser.parse(result);
                return jsonObject;
            }
        } catch (ParseException e) {
            log.error("JSON 변환 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("XML 변환 오류: {}", e.getMessage());
        }
        return jsonObject;
        //return result;
    }

    public static JSONObject requestApiFromFile(String Url, String path, MultiValueMap<String, String> params, DataType resultType) {

        WebClient webClient = WebClient.builder()
                .baseUrl(Url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();

        Path tempFile;
        try {
            tempFile = Files.createTempFile("api-response-", resultType == DataType.DATA_XML ? ".xml" : ".json");
        } catch (IOException e) {
            log.error("파일 생성 실패", e);
            throw new RuntimeException("파일 생성 실패", e);
        }

        Flux<DataBuffer> dataBufferFlux = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParams(params)
                        .build())
                .headers(headers -> {
                    if (Url.contains("data-dbg.krx.co.kr")) {
                        log.info("KRX KEY = [{}]",StockConstant.KRX_API_KEY);
                        headers.add("AUTH_KEY", StockConstant.KRX_API_KEY);
                    } else if (Url.contains("opendart.fss.or.kr")) {
                        log.info("DART KEY = [{}]",StockConstant.DART_KEY);
                        headers.add("CRTFC_KEY", StockConstant.DART_KEY);
                    }
                })
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE)) {
            Mono<Void> writeMono = DataBufferUtils.write(dataBufferFlux, channel, 0).then();
            writeMono.block();// 전체 스트림을 파일로 저장
        } catch (IOException e) {
            log.error("파일 저장 중 오류", e);
            throw new RuntimeException("파일 저장 실패", e);
        }

        // 저장된 파일 내용 읽기
        String result;
        try {
            result = Files.readString(tempFile);
        } catch (IOException e) {
            log.error("파일 읽기 실패", e);
            throw new RuntimeException("파일 읽기 실패", e);
        }

        if (result == null || result.isEmpty()) {
            log.error("Empty response from API");
            throw new RuntimeException("Empty response from API");
        }

        //log.info("응답 결과:\n{}", result);

        JSONObject jsonObject = null;
        try {
            if (resultType == DataType.DATA_XML) {
                // jsonObject = XML.toJSONObject(result);
            } else {
                JSONParser parser = new JSONParser();
                jsonObject = (JSONObject) parser.parse(result);
            }
        } catch (ParseException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
        } catch (Exception e) {
            log.error("파일 변환 오류: {}", e.getMessage());
        }

        return jsonObject;
    }

}
