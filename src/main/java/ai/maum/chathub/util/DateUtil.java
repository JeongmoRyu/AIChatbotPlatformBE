package ai.maum.chathub.util;

import ai.maum.chathub.meta.DateMeta;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DateUtil {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DateMeta.DATE_FORMAT);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateMeta.DATE_TIME_FORMAT_NORMAL);
    private static final DateTimeFormatter dateTimeShortFormatter = DateTimeFormatter.ofPattern(DateMeta.DATE_TIME_FORMAT_SHORT);
    private static final DateTimeFormatter dateTimeFgtFormatter = DateTimeFormatter.ofPattern(DateMeta.DATE_TIME_FORMAT_FGT);

    private DateUtil() {}

    /**
     * ms 단위 시간을 날짜 형식의 문자열로 변환한다.
     * @param time ms 단위의 시간
     * @return 날짜 형식의 문자열(yyyy-MM-dd HH:mm:ss)
     * @author baekgol
     */
    public static String convertToStringByMs(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(dateTimeFormatter);
    }

    public static String convertToShortStringByMS(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(dateTimeShortFormatter);
    }

    public static String convertToFGTStringByMS(long time) {
        String rtnString = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(dateTimeFgtFormatter);
        if(rtnString.endsWith("00분"))
            rtnString = rtnString.replaceAll("00분", "");

        return rtnString.trim();
    }
    /**
     * 날짜 형식의 문자열을 LocalDate 객체로 변환한다.
     * @param date 날짜(yyyy-MM-dd)
     * @return 입력한 날짜의 LocalDate 객체
     * @author baekgol
     */
    public static LocalDate convertToDateByString(String date) {
        return LocalDate.parse(date, dateFormatter);
    }

    /**
     * 날짜 및 시간 형식의 문자열을 LocalDateTime 객체로 변환한다.
     * @param dateTime 날짜 및 시간(yyyy-MM-dd HH:mm:ss)
     * @return 입력한 날짜의 LocalDateTime 객체
     * @author baekgol
     */
    public static LocalDateTime convertToDateTimeByString(String dateTime) {
        return LocalDateTime.parse(dateTime, dateTimeFormatter);
    }

    /**
     * LocalDate 객체를 날짜 형식의 문자열로 변환한다.
     * @param date 날짜
     * @return 날짜 형식의 문자열(yyyy-MM-dd)
     * @author baekgol
     */
    public static String convertToStringByDate(LocalDate date) {
        return date.format(dateTimeFormatter);
    }

    /**
     * LocalDateTime 객체를 날짜 및 시간 형식의 문자열로 변환한다.
     * @param dateTime 날짜 및 시간
     * @return 날짜 및 시간 형식의 문자열(yyyy-MM-dd HH:mm:ss)
     * @author baekgol
     */
    public static String convertToStringByDateTime(LocalDateTime dateTime) {
        return dateTime.format(dateTimeFormatter);
    }

    /**
     * 초 단위 시간을 시간 형식의 문자열로 변환한다.
     * ms 소수점 한 자리까지 표시한다.
     * @param time 초 단위 시간
     * @return 시간 형식의 문자열(HH:mm:ss.S)
     * @author baekgol
     */
    public static String convertToStringBySecond(double time) {
        double target = time;
        int hour = 0;
        int minute = 0;
        double second = 0;
        int ms = 0;

        if(target >= 3600) {
            hour = (int)(target / 3600);
            target %= 3600;
        }

        if(target >= 60) {
            minute = (int)(target / 60);
            target %= 60;
        }

        second = target;
        ms = (int)((second - (int)second) * 10);

        return String.format("%02d:%02d:%02d.%01d", hour, minute, (int)second, ms);
    }

    public static Map<String, Timestamp> convertDateStringToTimestampMap(String dateString) {

        try {

            Map<String, Timestamp> rtnMap = new HashMap<String, Timestamp>();
            // 입력으로 받은 날짜 (예: "2024/06/14")
            // 입력 날짜를 LocalDate로 변환
            // / -> - 변환
            dateString = dateString.replaceAll("/", "-");
            LocalDate localDate = LocalDate.parse(dateString);

            // 시작 시간 (00:00:00.000)
            LocalTime startTime = LocalTime.of(0, 0, 0, 0);
            LocalDateTime startDateTime = LocalDateTime.of(localDate, startTime);

            // 종료 시간 (23:59:59.000)
            LocalTime endTime = LocalTime.of(23, 59, 59, 0);
            LocalDateTime endDateTime = LocalDateTime.of(localDate, endTime);

            // 시작 시간과 종료 시간을 timestamp로 변환
            ZoneId kstZoneId = ZoneId.of("Asia/Seoul");
            long startValue = startDateTime.atZone(kstZoneId).toInstant().toEpochMilli();
            long endValue = endDateTime.atZone(kstZoneId).toInstant().toEpochMilli();

            Timestamp ts = new Timestamp(System.currentTimeMillis());

            rtnMap.put("startDate", new Timestamp(startValue));
            rtnMap.put("endDate", new Timestamp(endValue));

            return rtnMap;
        } catch (Exception e) {


            return null;
        }
    }

    public static String getTodayString() {
        LocalDate today = LocalDate.now();
        return today.format(dateFormatter);
    }
}
