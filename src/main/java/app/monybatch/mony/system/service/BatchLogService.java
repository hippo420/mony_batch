package app.monybatch.mony.system.service;

import app.monybatch.mony.common.constant.JobDomain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 실행 로그 1차 확인용 서비스 — 도메인 로그 파일(dart.log 등)의 tail에서
 * 해당 Job 라인만 추려서 반환. 상세 분석은 Kibana 딥링크로 유도한다.
 */
@Slf4j
@Service
public class BatchLogService {

    /** 파일 끝에서 읽어올 최대 바이트 (로그 파일은 10MB 롤링이므로 tail 2MB면 충분) */
    private static final int TAIL_BYTES = 2 * 1024 * 1024;

    @Value("${logging.file.path:./logs/mony_batch}")
    private String logPath;

    @Value("${monitoring.kibana.base-url:}")
    private String kibanaBaseUrl;

    public Map<String, Object> getExecutionLogs(String jobName, Long jobInstanceId,
                                                LocalDateTime startTime, LocalDateTime endTime, int lines) {
        String domain = JobDomain.resolve(jobName);
        Path file = Path.of(logPath, domain + ".log");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("domain", domain);
        result.put("file", file.getFileName().toString());
        result.put("lines", readMatchingTail(file, jobName, Math.min(Math.max(lines, 1), 2000)));
        result.put("kibanaUrl", buildKibanaUrl(jobName, jobInstanceId, startTime, endTime));
        return result;
    }

    /**
     * 파일 끝 TAIL_BYTES 안에서 해당 Job의 로그 라인만 추출.
     * '['로 시작하지 않는 줄(스택트레이스 등 연속 라인)은 직전 매칭 라인에 이어붙는 것으로 간주.
     */
    private List<String> readMatchingTail(Path file, String jobName, int maxLines) {
        if (!Files.isReadable(file)) {
            return List.of("(로그 파일이 없습니다: " + file + ")");
        }
        try (SeekableByteChannel channel = Files.newByteChannel(file)) {
            long size = channel.size();
            int readBytes = (int) Math.min(size, TAIL_BYTES);
            channel.position(size - readBytes);
            ByteBuffer buffer = ByteBuffer.allocate(readBytes);
            while (buffer.hasRemaining() && channel.read(buffer) > 0) {
                // 버퍼가 찰 때까지 읽기
            }
            String text = new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8);

            String[] allLines = text.split("\n", -1);
            // 파일 중간부터 읽었으면 첫 줄은 잘린 라인이므로 버림
            int startIdx = (size > readBytes) ? 1 : 0;

            String marker = "[" + jobName + "]";
            List<String> matched = new ArrayList<>();
            boolean inMatch = false;
            for (int i = startIdx; i < allLines.length; i++) {
                String line = allLines[i];
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("[")) {
                    inMatch = line.contains(marker);
                }
                if (inMatch) {
                    matched.add(line);
                }
            }
            if (matched.size() > maxLines) {
                matched = matched.subList(matched.size() - maxLines, matched.size());
            }
            return matched;
        } catch (IOException e) {
            log.warn("로그 파일 읽기 실패: {}", file, e);
            return List.of("(로그 파일 읽기 실패: " + e.getMessage() + ")");
        }
    }

    /** Kibana Discover 딥링크 — base-url 미설정 시 null (화면에서 버튼 숨김) */
    private String buildKibanaUrl(String jobName, Long jobInstanceId,
                                  LocalDateTime startTime, LocalDateTime endTime) {
        if (!StringUtils.hasText(kibanaBaseUrl)) {
            return null;
        }
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String from = (startTime != null) ? startTime.minusMinutes(1).format(iso) : "now-1d";
        String to = (endTime != null) ? endTime.plusMinutes(1).format(iso) : "now";

        String query = "JOB_NAME:%22" + jobName + "%22";
        if (jobInstanceId != null) {
            query += "%20and%20JOB_INSTANCE_ID:%22" + jobInstanceId + "%22";
        }
        return kibanaBaseUrl + "/app/discover#/?_g=(time:(from:'" + from + "',to:'" + to + "'))"
                + "&_a=(query:(language:kuery,query:'" + query + "'))";
    }
}
