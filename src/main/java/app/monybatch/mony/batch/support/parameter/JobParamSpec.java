package app.monybatch.mony.batch.support.parameter;

/**
 * Job 실행 파라미터 명세 — 모니터링 화면의 실행 모달이 라벨/형식 힌트/기본값으로 사용.
 *
 * @param key          JobParameters 키
 * @param label        화면 표시용 라벨
 * @param format       입력 형식 힌트 (예: yyyyMMdd)
 * @param defaultValue 기본값 (없으면 빈 문자열)
 * @param required     필수 여부
 */
public record JobParamSpec(String key, String label, String format, String defaultValue, boolean required) {
}
