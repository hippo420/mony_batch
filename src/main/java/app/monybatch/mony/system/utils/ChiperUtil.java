package app.monybatch.mony.system.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class ChiperUtil {

    public static String sha256(String pw) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(pw.getBytes());
        } catch(Exception e) {
            e.printStackTrace();
        }
        byte[] pwd = md.digest();  // 배열 pwd : 문자열 str이 암호화된 32바이트 크기의 배열
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < pwd.length; i++) {
            sb.append(String.format("%2X", pwd[i])); // %2X(2자리 16진수(0~F))
        }
        return sb.toString();
    }

    public static String sha512(String pw) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(pw.getBytes());
        } catch(Exception e) {
            e.printStackTrace();
        }
        byte[] pwd = md.digest();  // 배열 pwd : 문자열 str이 암호화된 32바이트 크기의 배열
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < pwd.length; i++) {
            sb.append(String.format("%2X", pwd[i])); // %2X(2자리 16진수(0~F))
        }
        return sb.toString();
    }

    public static String getSalt(){

        // 보안 이슈로 Random이 아닌 SecureRandom을 사용해 주는것이 좋다.
        SecureRandom secureRandom = new SecureRandom();

        byte[] salt = new byte[20];
        secureRandom.nextBytes(salt); // 난수 생성

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < salt.length; i++) {
            sb.append(String.format("%2X", salt[i]));
        }
        return sb.toString();
    }

    public static String sha256Salt(String pw, String salt) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update((pw + salt).getBytes());
        } catch(Exception e) {
            e.printStackTrace();
        }
        byte[] pwSalt = md.digest();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < pwSalt.length; i++) {
            sb.append(String.format("%2X", pwSalt[i]));
        }
        return sb.toString();
    }
}
