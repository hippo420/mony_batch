package app.monybatch.mony.system.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtil {
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

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
    };
}
