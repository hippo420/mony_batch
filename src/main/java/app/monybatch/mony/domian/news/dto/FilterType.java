package app.monybatch.mony.domian.news.dto;

public enum FilterType {
    RULE_WHITELIST,  // 정규식 화이트리스트 단어 매칭으로 통과됨
    LLM_PASSED,      // 규칙에는 안 걸렸지만, LLM이 판단해서 통과됨
    BYPASSED         // (필요 시) 수동이나 기타 예외 조건으로 통과됨
}
