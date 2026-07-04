package app.monybatch.mony.common.core.utils;

import app.monybatch.mony.domian.earning.dto.ReportQuarter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DateUtil {
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * Date 객체를 원하는 포맷의 문자열로 변환
     * @param date 변환할 날짜
     * @param format 포맷 (예: "yyyy-MM-dd HH:mm:ss")
     */
    public static String format(Date date, String format) {
        if (date == null) return null;

        // Date -> LocalDateTime 변환
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 문자열을 Date 객체로 변환 (공시 API 연동 시 유용)
     */
    public static Date parse(String dateStr, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /*
     @Method :
     @Param :
     @Desc :
     */
    public static String getDateYmd(){
        LocalDate now = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMMDD);

        return  now.format(formatter);
    };

    /*
     @Method :
     @Param :
     @Desc :
     */
    public static String getDateYmd(String format){
        LocalDate now = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        return  now.format(formatter);
    };

    /*
     @Method :
     @Param :
     @Desc :
     */
    public static String getFormatDate(String date) {
        if (date.length() == 8) {
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
        }
        return date;
    }


    /*
     @Method : getDateMilli
     @Param :
     @Desc :
     */
    public static String getDateMilli(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMMDDHHMMSS);

        return  now.format(formatter);
    };

    /*
     @Method : getDateYm
     @Param :
     @Desc :
     */
    public static String getDateYm(){
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMM);

        return  now.format(formatter);
    };

    /*
     @Method : getDateYm
     @Param :
     @Desc :
     */
    public static List<String> getDateListToToday(String startDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        LocalDate startDate = LocalDate.parse(startDateStr, formatter).plusDays(1);
        LocalDate endDate = LocalDate.now();

        List<String> dateList = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            dateList.add(current.format(formatter));
            current = current.plusDays(1);
        }

        return dateList;
    }

    /*
     @Method : getDateYm
     @Param :
     @Desc :
     */
    public static String getMinusDay(String curYmd,int days){
        // 1. yyyyMMdd 형식의 포맷터 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMMDD);

        try {
            // 2. 입력받은 문자열을 LocalDate로 변환
            LocalDate date = LocalDate.parse(curYmd, formatter);

            // 3. 날짜 차감 계산
            LocalDate resultDate = date.minusDays(days);

            // 4. 다시 yyyyMMdd 형식의 문자열로 변환하여 반환
            return resultDate.format(formatter);
        } catch (Exception e) {
            // 날짜 형식이 잘못되었을 경우 예외 처리
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다 (yyyyMMdd 필요): " + curYmd);
        }
    }

    public static LocalDateTime parseToLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignored) {
        }

        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
        }

        return LocalDateTime.now();
    }

    public static String toStringDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static String getCurrentYear() {
        return String.valueOf(LocalDate.now().getYear());
    }

    // offset: -1=전년도, 0=현재, 1=내년도
    public static String getCurrentYearByInt(int offset) {
        return String.valueOf(LocalDate.now().getYear() + offset);
    }

    public static ReportQuarter getCurrentReportQuarter() {
        int month = LocalDate.now().getMonthValue();
        if (month <= 3) return ReportQuarter.Q1;
        if (month <= 6) return ReportQuarter.Q2;
        if (month <= 9) return ReportQuarter.Q3;
        return ReportQuarter.Q4;
    }

    // offset: -1=전분기, 0=현재, 1=다음분기 (1~4 순환)
    public static ReportQuarter getCurrentReportQuarterByInt(int offset) {
        int month = LocalDate.now().getMonthValue();
        int current;
        if (month <= 3) current = 1;
        else if (month <= 6) current = 2;
        else if (month <= 9) current = 3;
        else current = 4;
        int result = ((current - 1 + offset) % 4 + 4) % 4 + 1;
        return switch (result) {
            case 1 -> ReportQuarter.Q1;
            case 2 -> ReportQuarter.Q2;
            case 3 -> ReportQuarter.Q3;
            default -> ReportQuarter.Q4;
        };
    }



    /**
     * 특정 시작일자부터 현재일자까지의 날짜 목록을 YYYYMMDD 포맷의 리스트로 반환합니다.
     *
     * @param startDateStr 시작 일자 (YYYYMMDD)
     * @return 날짜 문자열 리스트
     */
    public static List<String> getDatesUntilToday(String startDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate today = LocalDate.now();

        if (startDate.isAfter(today)) return new ArrayList<>();


        return startDate.datesUntil(today.plusDays(1))
                .map(date -> date.format(formatter))
                .collect(Collectors.toList());
    }
}
