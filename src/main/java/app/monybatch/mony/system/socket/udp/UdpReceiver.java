package app.monybatch.mony.system.socket.udp;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Component
@Slf4j
public class UdpReceiver {
    private static final int PORT = 9999;

    private final StringRedisTemplate redisTemplate;

    public UdpReceiver(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void start() {
        Thread thread = new Thread(this::listen);
        thread.setDaemon(true);
        thread.start();
    }

    private void listen() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[2048];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String data = new String(packet.getData(), 0, packet.getLength(), "UTF-8");

                // 데이터 가공
                String processed = processData(data);

                // Redis에 저장
                saveToRedis(processed);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String processData(String data) {
        // 예: JSON → 특정 필드만 저장
        return data.trim();
    }

    private void saveToRedis(String processed) {
        // 1초마다 들어오므로 리스트 형태로 적재
        redisTemplate.opsForList().leftPush("UDP_DATA_LIST", processed);
    }
}
