package app.monybatch.mony.business.constant;

import java.util.Arrays;
import java.util.Optional;

public enum DartCode {
    // Enum 상수 정의: 반드시 맨 위에 위치하며, 세미콜론(;)으로 끝나야 합니다.
    SUCCESS("000", "정상"),

    // 01X (키/접근 관련)
    UNREGISTERED_KEY("010", "등록되지 않은 키입니다."),
    DISABLED_KEY("011", "사용할 수 없는 키입니다. 오픈API에 등록되었으나, 일시적으로 사용 중지된 키를 통하여 검색하는 경우 발생합니다."),
    ACCESS_DENIED_IP("012", "접근할 수 없는 IP입니다."),
    NO_DATA("013", "조회된 데이타가 없습니다."),
    FILE_NOT_EXIST("014", "파일이 존재하지 않습니다."),

    // 02X (제한 관련)
    REQUEST_LIMIT_EXCEEDED("020", "요청 제한을 초과하였습니다."),
    COMPANY_LIMIT_EXCEEDED("021", "조회 가능한 회사 개수가 초과하였습니다.(최대 100건)"),

    // 1XX (부적절한 접근/값)
    IMPROPER_FIELD_VALUE("100", "필드의 부적절한 값입니다. 필드 설명에 없는 값을 사용한 경우에 발생하는 메시지입니다."),
    IMPROPER_ACCESS("101", "부적절한 접근입니다."),

    // 기타
    SYSTEM_CHECK("800", "시스템 점검으로 인한 서비스가 중지 중입니다."),
    UNDEFINED_ERROR("900", "정의되지 않은 오류가 발생하였습니다."),
    KEY_EXPIRATION("901", "사용자 계정의 개인정보 보유기간이 만료되어 사용할 수 없는 키입니다. 관리자 이메일(opendart@fss.or.kr)로 문의하시기 바랍니다.");

    // ----------------------------------------------------
    // 인스턴스 변수 및 생성자
    // ----------------------------------------------------

    private final String code;
    private final String message;

    // Enum 생성자는 private으로만 선언 가능합니다.
    DartCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    // ----------------------------------------------------
    // Getter 메서드
    // ----------------------------------------------------

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    // ----------------------------------------------------
    // 정적 헬퍼 메서드 (코드로 Enum을 찾는 기능)
    // ----------------------------------------------------

    /**
     * 문자열 오류 코드를 사용하여 해당 ErrorCode Enum 상수를 찾습니다.
     * @param code 찾고자 하는 오류 코드 문자열
     * @return 해당 ErrorCode, 찾지 못하면 Optional.empty()
     */
    public static Optional<DartCode> fromCode(String code) {
        return Arrays.stream(DartCode.values())
                .filter(errorCode -> errorCode.code.equals(code))
                .findFirst();
    }
}
