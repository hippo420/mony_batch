package app.monybatch.mony.business.service;

import app.monybatch.mony.business.constant.StockConstant;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisTokenManager {

    private final StringRedisTemplate redisTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${apikey.kis.dev.app.key}")
    private String appKey;

    @Value("${apikey.kis.dev.app.secret}")
    private String appSecret;

    private static final String REDIS_KEY_PREFIX = "KIS_ACCESS_TOKEN";
    private static final long TOKEN_TTL_SECONDS = 86400; // 24시간

    @PostConstruct
    public void init() {
        log.info("Initializing KIS Token Manager...");
        try {
             issueToken();
        } catch (Exception e) {
            log.error("Failed to issue token during initialization", e);
        }
    }

    @Scheduled(fixedRate = TOKEN_TTL_SECONDS * 1000) // 24시간마다 실행
    public void refreshToken() {
        log.info("Refreshing KIS Access Token...");
        issueToken();
    }

    @PreDestroy
    public void destroy() {
        log.info("Destroying KIS Token Manager...");

        //revokeToken();
        //redisTemplate.delete(REDIS_KEY_PREFIX);
    }

    public String getAccessToken() {
        String token = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX);
        if (token == null) {
            log.warn("Token not found in Redis, re-issuing...");
            //issueToken();
            token = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX);
        }
        return token;
    }

    private void issueToken() {
        String token = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX);
        if(token != null)
            return;
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "client_credentials");
        requestBody.put("appkey", appKey);
        requestBody.put("appsecret", appSecret);
        log.info("appkey: {}", appKey);
        log.info("appsecret: {}", appSecret);
        WebClient webClient = webClientBuilder.baseUrl(StockConstant.KIS_API_URL).build();

        try {
            Map response = webClient.post()
                    .uri(StockConstant.KIS_TOKEN_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("access_token")) {
                String accessToken = (String) response.get("access_token");
                String tokenType = (String) response.get("token_type"); // "Bearer"
                String fullToken = tokenType + " " + accessToken;

                // Redis에 저장 (TTL 설정)
                redisTemplate.opsForValue().set(REDIS_KEY_PREFIX, fullToken, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
                log.info("KIS Access Token issued and stored in Redis. Expires in {} seconds.", TOKEN_TTL_SECONDS);
            } else {
                log.error("Failed to issue KIS Access Token: Invalid response from API");
            }
        } catch (WebClientResponseException e){
            // ✅ 여기서 서버가 보낸 실제 에러 내용을 확인할 수 있습니다.
            String errorBody = e.getResponseBodyAsString();
            String decodedMessage = new String(errorBody.getBytes(Charset.forName("EUC-KR")));
            log.error("### KIS API 에러 발생 ###");
            log.error("상태 코드: {}", e.getStatusCode()); // 403
            log.error("에러 메시지: {}", decodedMessage);      // 상세 이유 (JSON)
        } catch (Exception e) {
            log.error("Error issuing KIS Access Token: {}", e.getMessage());
        }
    }

    private void revokeToken() {
        String fullToken = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX);
        if (fullToken == null) {
            log.warn("No token found in Redis to revoke.");
            return;
        }

        // "Bearer " 제거
        String token = fullToken.startsWith("Bearer ") ? fullToken.substring(7) : fullToken;

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("appkey", appKey);
        requestBody.put("appsecret", appSecret);
        requestBody.put("token", token);

        WebClient webClient = webClientBuilder.baseUrl(StockConstant.KIS_API_URL).build();

        try {
            Map response = webClient.post()
                    .uri(StockConstant.KIS_REVOKE_PATH)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && "200".equals(String.valueOf(response.get("code")))) {
                log.info("KIS Access Token revoked successfully.");
            } else {
                log.warn("Failed to revoke KIS Access Token. Response: {}", response);
            }
        } catch (Exception e) {
            log.error("Error revoking KIS Access Token", e);
        }
    }
}
