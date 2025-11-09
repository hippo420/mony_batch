package app.monybatch.mony.system.utils;

import app.monybatch.mony.business.constant.StockConstant;
import app.monybatch.mony.business.entity.dart.XMLResult;
import app.monybatch.mony.system.core.constant.DataType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;


@Slf4j
public class OpenAPIUtil {


    public static JSONObject requestApi(String Url, String path, MultiValueMap<String,String> params, DataType resultType)  {


        WebClient webClient = WebClient.builder()
                .baseUrl(Url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        //log.info("===================OPEN_API START===================");
        if (Url.contains("opendart.fss.or.kr")) {
            params.add("crtfc_key", StockConstant.DART_KEY);
        }

        String result =  webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParams(params)
                        .build())
                .headers(headers -> {
                    if (Url.contains("data-dbg.krx.co.kr")) {
                        headers.add("AUTH_KEY", StockConstant.KRX_API_KEY);
                    } else if (Url.contains("opendart.fss.or.kr")) {
                        //headers.add("CRTFC_KEY", StockConstant.DART_KEY);
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

        if (Url.contains("opendart.fss.or.kr")) {
            params.add("crtfc_key", StockConstant.DART_KEY);
        }

        WebClient webClient = WebClient.builder()
                .baseUrl(Url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();

        Path tempFile;
        try {

            String extension = ".txt";
            switch (resultType)
            {
                case DataType.DATA_XML:
                    extension = ".xml";
                    break;
                case DataType.DATA_JSON:
                    extension = ".json";
                    break;
                case DataType.DATA_ZIP:
                    extension = ".zip";
                    break;
                default:
                    extension = ".txt";
                    break;
            }

            tempFile = Files.createTempFile("api-response-", extension);
            log.info("임시파일생성 : {}",tempFile.toString());
        } catch (IOException e) {
            log.error("파일 생성 실패", e);
            throw new RuntimeException("파일 생성 실패", e);
        }
        log.info("URL: {}",Url+path);
        params.entrySet().stream().forEach(entry -> {log.info("key: {}",entry.getKey());log.info("value: {}",entry.getValue());});
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

        //압축파일의 경우 압출해제후 파일 읽기
        if(resultType == DataType.DATA_ZIP) {
            // System.getProperty()를 사용하여 임시 디렉터리 경로를 가져옵니다.
            String tempDirPath = System.getProperty("java.io.tmpdir");
            try {
                String unzipFile = ZipUtil.unzip(tempFile.toAbsolutePath().toString(), tempDirPath);
                //CORPCODE.xml
                tempFile= Path.of(unzipFile,"CORPCODE.xml");
            }
            catch (FileNotFoundException e) {
                log.error("파일을 찾을수 없음 :{}", tempFile.toAbsolutePath());
            }
            catch (Exception e) {
                log.error("압축해제중 오류발생 :{}", e.getMessage());
            }
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
            if (resultType == DataType.DATA_XML || resultType == DataType.DATA_ZIP) {
                XmlMapper mapper = new XmlMapper();
                XMLResult xmLData = mapper.readValue(result, XMLResult.class);
                //log.info("XMLResult : {}",xMLData);

                //STockCode없으면 제외처리
                if (xmLData.getList() != null) {
                    xmLData.setList(
                            xmLData.getList().stream()
                                    .filter(item -> item.getStock_code() != null && !item.getStock_code().trim().isEmpty())
                                    .collect(Collectors.toList())
                    );
                }
                ObjectMapper jsonMapper = new ObjectMapper();
                String json = jsonMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(xmLData);
                JSONParser parser = new JSONParser();
                jsonObject = (JSONObject) parser.parse(json);
                return jsonObject;

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
