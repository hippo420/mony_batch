package app.monybatch.mony.domian.earning.dto;

public enum ReportQuarter {
    Q1("1분기"),
    Q2("2분기"),
    Q3("3분기"),
    Q4("4분기"),
    YEAR("연간"); // 연간 연결/별도 실적 공시용

    private final String description;

    ReportQuarter(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
