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
     @Method
     @Desc
     */
    public static String getDateYmd(){
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMMDD);

        return  now.format(formatter);
    };

    /*
     @Method
     @Desc
     */
    public static String getDateMilli(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMMDDHHMMSS);

        return  now.format(formatter);
    };

    /*
     @Method
     @Desc
     */
    public static String getDateYm(){
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMM);

        return  now.format(formatter);
    };

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
}
