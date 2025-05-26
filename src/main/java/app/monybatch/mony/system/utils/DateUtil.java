package app.monybatch.mony.system.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
}
