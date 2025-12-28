package app.monybatch.mony.system.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    /**
     * 입력된 문자열(URL)을 MD5 해시 문자열로 변환합니다.
     * 이 해시 값을 Elasticsearch의 _id로 사용합니다.
     * * @param input URL 문자열
     *
     * @return MD5 해시 문자열 (32자리)
     */
    public static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();

            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                // 각 바이트를 16진수 2자리로 변환 (패딩 포함)
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5 알고리즘이 시스템에 없을 경우의 예외 처리
            throw new RuntimeException("MD5 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
