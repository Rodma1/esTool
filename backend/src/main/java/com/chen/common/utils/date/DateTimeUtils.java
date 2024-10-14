package com.chen.common.utils.date;

import com.chen.common.utils.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Chennong
 * @date 2022/3/22
 * @description 日期时间工具类
 */
public class DateTimeUtils {

    /**
     * 年，格式：yyyy
     */
    public static final String y4 = "yyyy";

    /**
     * 日，格式：MM
     */
    public static final String m2 = "MM";

    /**
     * 日，格式：dd
     */
    public static final String d2 = "dd";

    /**
     * 年月，格式：yyyy-MM
     */
    public static final String y4M2 = "yyyy-MM";

    /**
     * 年月日，格式：yyyy-MM-dd
     */
    public static final String y4M2d2 = "yyyy-MM-dd";

    /**
     * 年月，格式：yyyyMM
     */
    public static final String y4M2_NON = "yyyyMM";

    /**
     * 日月，格式：yyyyMM
     */
    public static final String M2d2 = "MMdd";

    /**
     * 年月日，格式：yyyyMMdd
     */
    public static final String y4M2d2_NON = "yyyyMMdd";

    /**
     * 年月日时分，格式：yyyy-MM-dd HH:mm
     */
    public static final String y4M2d2H2m2 = "yyyy-MM-dd HH:mm";

    /**
     * 年月日时分秒，格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String y4M2d2H2m2s2 = "yyyy-MM-dd HH:mm:ss";

    /**
     * 年月日时分秒毫秒，格式：yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final String y4M2d2H2m2s2S3 = "yyyy-MM-dd HH:mm:ss.SSS";
    /**
     * 年月日时分秒毫秒，格式：yyyy-MM-dd HH:mm:ss.S
     */
    public static final String y4M2d2H2m2s2S = "yyyy-MM-dd HH:mm:ss.S";
    /**
     * 年月日时，格式：yyyyMMddHH
     */
    public static final String y4M2d2H2_NON = "yyyyMMddHH";

    /**
     * 年月日时，格式：yyMMddHH
     */
    public static final String y2M2d2H2_NON = "yyMMddHH";

    /**
     * 年月日时，格式：yyMMddHHmm
     */
    public static final String y2M2d2H2m2_NON = "yyMMddHHmm";

    /**
     * 年月日时分秒，格式：yyyyMMddHHmmss
     */
    public static final String y4M2d2H2m2s2_NON = "yyyyMMddHHmmss";

    /**
     * 年月日时分秒毫秒，格式：yyyyMMddHHmmssSSS
     */
    public static final String y4M2d2H2m2s2S3_NON = "yyyyMMddHHmmssSSS";

    /**
     * 时分秒，格式：HH:mm:ss
     */
    public static final String H2m2s2 = "HH:mm:ss";

    /**
     * 时分秒，格式：HHmmss
     */
    public static final String H2m2s2_NON = "HHmmss";

    /**
     * 秒毫秒，格式：ssSSS
     */
    public static final String s2S3_NON = "ssSSS";

    /**
     * 日期运算符：相加（加法）
     */
    public static final String DATE_OPERATOR_ADD = "add";

    /**
     * 日期运算符：相减（减法）
     */
    public static final String DATE_OPERATOR_SUB = "sub";

    public static final long SECOND = 1000L;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;
    public static final long WEEK = 7 * DAY;

    /**
     * 校验指定的日期字符串是否合法
     *
     * @param dateTimeValue 字符串形式的日期时间值
     * @param pattern       符合哪种日期时间格式
     */
    public static boolean isDateTime(String dateTimeValue, String pattern) {
        if (StringUtils.isBlank(dateTimeValue)) {
            return false;
        }
        try {
            new SimpleDateFormat(pattern).parse(dateTimeValue);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 获取当天开始0点起的时间戳
     */
    public static String getTodayStartTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, 0);
        return sdf.format(calendar.getTime());
    }

    /**
     * 计算给定日期字符串第二天的日期
     *
     * @param today 格式为yyyy-MM-dd的字符串
     * @return 第二天的yyyy-MM-dd字符串
     */
    public static String getTomorrowDate(String today) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tomorrow = null;
        try {
            Date date = sdf.parse(today);
            Long tomorrowTime = date.getTime() + 24L * 60 * 60 * 1000;
            date.setTime(tomorrowTime);
            tomorrow = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tomorrow;
    }

    /**
     * 计算给定日期字符串昨天的日期
     *
     * @param today 格式为yyyy-MM-dd的字符串
     * @return 昨天的yyyy-MM-dd字符串
     */
    public static String getYesterdayDate(String today) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tomorrow = null;
        try {
            Date date = sdf.parse(today);
            Long tomorrowTime = date.getTime() - 24L * 60 * 60 * 1000;
            date.setTime(tomorrowTime);
            tomorrow = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tomorrow;
    }

    /**
     * 返回当前日期时间
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateTimeUtils.y4M2d2H2m2s2);
        return simpleDateFormat.format(new Date());
    }

    /**
     * 返回当前日期时间
     *
     * @param pattern
     * @return 指定格式的当前日期时间
     */
    public static String getCurrentDateTime(String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.format(new Date());
        } else {
            return getCurrentDateTime();
        }
    }

    /**
     * 获取昨天时间
     *
     * @param pattern 指定日期格式
     * @return 指定格式的昨天日期时间
     */
    public static String getYesterdayDateTime(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.format(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 以当前日期时间为基础，计算自定义的日期时间
     *
     * @param days      自定义天数
     * @param operator  运算符，格式：add-加法；sub-减法。
     * @param toPattern 自定义的日期时间格式，格式：yyyy-MM-dd、yyyyMMdd等
     * @return 返回自定义的日期时间
     */
    public static String getDateTimeByCustomDays(long days, String operator, String toPattern) {
        return getDateTimeByCustom(days * 24, 0, 0, operator, toPattern);
    }

    /**
     * 以当前日期时间为基础，计算自定义的日期时间
     *
     * @param hours     小时数
     * @param operator  运算符，格式：add-加法；sub-减法。
     * @param toPattern 自定义的日期时间格式，格式：yyyy-MM-dd、yyyyMMdd等
     * @return 返回自定义的日期时间
     */
    public static String getDateTimeByCustomHours(long hours, String operator, String toPattern) {
        return getDateTimeByCustom(hours, 0, 0, operator, toPattern);
    }

    /**
     * 以当前日期时间为基础，计算自定义的日期时间
     *
     * @param hours     小时数
     * @param minus     分钟数
     * @param seconds   秒钟数
     * @param operator  运算符，格式：add-加法；sub-减法。
     * @param toPattern 自定义的日期时间格式，格式：yyyy-MM-dd、yyyyMMdd等
     * @return 返回自定义的日期时间
     */
    public static String getDateTimeByCustom(long hours, long minus, long seconds, String operator, String toPattern) {
        return getDateTimeByCustom(new Date(), hours, minus, seconds, operator, toPattern);
    }

    public static String getDateTimeByCustom(Date date, long hours, long minus, long seconds, String operator, String toPattern) {
        Date tempDate = new Date();
        switch (operator) {
            case "add":
                tempDate.setTime(date.getTime() + (hours * 3600 + minus * 60 + seconds) * 1000);
                break;
            case "sub":
                tempDate.setTime(date.getTime() - (hours * 3600 + minus * 60 + seconds) * 1000);
                break;
            default:
                break;
        }
        return new SimpleDateFormat(toPattern).format(tempDate);
    }

    public static Date getDateTimeByCalculate(Date date, long hours, long minus, long seconds, String operator, String toPattern) {
        Date tempDate = new Date();
        switch (operator) {
            case "add":
                tempDate.setTime(date.getTime() + (hours * 3600 + minus * 60 + seconds) * 1000);
                break;
            case "sub":
                tempDate.setTime(date.getTime() - (hours * 3600 + minus * 60 + seconds) * 1000);
                break;
            default:
                break;
        }
        return tempDate;
    }

    /**
     * @param dateTime 时间
     * @param num      加的数，-num就是减去
     * @return 减去相应的数量的月份的日期
     * @throws ParseException Date
     */
    public static String monthAddNum(Date dateTime, Integer num, String toPattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        calendar.add(Calendar.MONTH, num);
        Date newTime = calendar.getTime();
        return new SimpleDateFormat(toPattern).format(newTime);
    }


    /**
     * 字符串类型的日期时间对象转换成Date型 （格式必须一一对应）
     *
     * @param dateTime 待转换的日期时间实例     例如：dateTime:2018-02-20 10:00:00;
     * @param pattern  待转换的日期时间实例对应的格式    例如：pattern:"yyyy-MM-dd HH:mm:ss"
     * @return Date型的日期对象
     */
    public static Date toDate(String dateTime, String pattern) {
        Date date = null;
        try {
            date = new SimpleDateFormat(pattern).parse(dateTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return date;
    }

    /**
     * Date型的日期时间对象转换成指定的字符串日期格式
     *
     * @param dateTime  待转换的日期时间实例
     * @param toPattern 计划转换成的字符串格式
     * @return 指定格式的字符串类型日期对象
     */
    public static String toString(Date dateTime, String toPattern) {
        if (dateTime == null) {
            return null;
        }
        String dateTimeStr = null;
        try {
            dateTimeStr = new SimpleDateFormat(toPattern).format(dateTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dateTimeStr;
    }

    /**
     * Timestamp型的日期时间对象转换成指定的字符串日期格式
     *
     * @param timestamp 待转换的日期时间实例
     * @param toPattern 计划转换成的字符串格式
     * @return 指定格式的字符串类型日期对象
     */
    public static String toString(Timestamp timestamp, String toPattern) {
        if (timestamp == null) {
            return null;
        }
        String dateTimeStr = null;
        try {
            dateTimeStr = new SimpleDateFormat(toPattern).format(timestamp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dateTimeStr;
    }

    /**
     * 字符串型的日期时间对象转换成新制定的格式
     *
     * @param dateTimeValue 待转换的字符串型日期时间实例
     * @param pattern       当前的格式
     * @param toPattern     计划转换成的格式
     * @return 指定格式的字符串类型日期对象
     */
    public static String format(String dateTimeValue, String pattern, String toPattern) {
        if (dateTimeValue == null) {
            return null;
        }
        if (dateTimeValue.equals("")) {
            return "";
        }
        Date dateTimeDate = DateTimeUtils.toDate(dateTimeValue, pattern);
        try {
            return new SimpleDateFormat(toPattern).format(dateTimeDate);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 比较两个时间的大小(12小时制)
     *
     * @param time1
     * @param time2
     * @return
     * @throws ParseException
     * @throws ParseException
     * @throws Exception
     */
    public static boolean compareOf12H(String time1, String time2) throws ParseException {
        if (StringUtils.isEmpty(time1) || StringUtils.isEmpty(time2)) {
            return false;
        }
        // 如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        // 将字符串形式的时间转化为Date类型的时间
        Date datetime1 = sdf.parse(time1);
        Date datetime2 = sdf.parse(time2);
        // 根据将Date转换成毫秒
        if (datetime1.getTime() > datetime2.getTime()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 比较两个时间的大小(24小时制)
     *
     * @param time1
     * @param time2
     * @return
     * @throws ParseException
     * @throws ParseException
     * @throws Exception
     */
    public static boolean compareOf24H(String time1, String time2) throws ParseException {
        if (StringUtils.isEmpty(time1) || StringUtils.isEmpty(time2)) {
            return false;
        }
        // 如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 将字符串形式的时间转化为Date类型的时间
        Date datetime1 = sdf.parse(time1);
        Date datetime2 = sdf.parse(time2);
        // 根据将Date转换成毫秒
        if (datetime1.getTime() > datetime2.getTime()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 比较两个时间的大小
     *
     * @param time1
     * @param time2
     * @param pattern
     * @return
     * @throws Exception
     */
    public static boolean compare(String time1, String time2, String pattern) {
        if (StringUtils.isEmpty(time1) || StringUtils.isEmpty(time2)) {
            return false;
        }
        // 如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        // 将字符串形式的时间转化为Date类型的时间
        Date datetime1 = null;
        Date datetime2 = null;
        try {
            datetime1 = sdf.parse(time1);
            datetime2 = sdf.parse(time2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 根据将Date转换成毫秒
        if (datetime1.getTime() > datetime2.getTime()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置时间
     *
     * @param date   日期实例
     * @param hour   时
     * @param minute 分
     * @param second 秒
     * @return 设置后的日期时间
     */
    public static Date setTime(Date date, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 两个日期的间隔天数
     *
     * @param beginDate 起始日期
     * @param endDate   截止日期
     * @return 间隔天数
     */
    public static int getBetweenDays(Date beginDate, Date endDate) {
        return Math.abs(new Long((setTime(endDate, 0, 0, 0).getTime() - setTime(beginDate, 0, 0, 0).getTime()) / DAY).intValue());
    }

    /**
     * 两个日期的间隔分钟
     *
     * @param beginDate 起始日期
     * @param endDate   截止日期
     * @return 间隔分钟
     */
    public static int getBetweenMinutes(Date beginDate, Date endDate) {
        return Math.abs(new Long((endDate.getTime() - beginDate.getTime()) % DAY % HOUR / MINUTE).intValue());
    }

    /**
     * 获得指定日期范围内所有的日期
     *
     * @param beginDate 起始日期，格式[yyyyMMdd]
     * @param endDate   截止日期，格式[yyyyMMdd]
     * @return 日期列表
     */
    public static List<String> listDatesBetweenDate(String beginDate, String endDate) {
        List<String> dates = new ArrayList<String>();
        if (StringUtils.equals(beginDate, endDate)) {
            dates.add(beginDate);
        } else {
            Date beginDateDt = DateTimeUtils.toDate(beginDate, DateTimeUtils.y4M2d2_NON);
            Date endDateDt = DateTimeUtils.toDate(endDate, DateTimeUtils.y4M2d2_NON);
            int numbs = getBetweenDays(beginDateDt, endDateDt);
            String currentDate = null;
            for (int i = 0; i <= numbs; i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(beginDateDt);
                calendar.add(Calendar.DAY_OF_YEAR, i);
                try {
                    currentDate = new SimpleDateFormat(DateTimeUtils.y4M2d2_NON).format(calendar.getTime());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                dates.add(currentDate);
            }
        }
        return dates;
    }

    /**
     * 获得指定日期范围内所有的日期
     *
     * @param beginDate 起始日期，格式[pattern]
     * @param endDate   截止日期，格式[pattern]
     * @return 日期列表
     */
    public static List<String> listDatesBetweenDate(String beginDate, String endDate, String pattern) {
        List<String> dates = new ArrayList<String>();
        if (StringUtils.equals(beginDate, endDate)) {
            dates.add(beginDate);
        } else {
            Date beginDateDt = DateTimeUtils.toDate(beginDate, pattern);
            Date endDateDt = DateTimeUtils.toDate(endDate, pattern);
            int numbs = getBetweenDays(beginDateDt, endDateDt);
            String currentDate = null;
            for (int i = 0; i <= numbs; i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(beginDateDt);
                calendar.add(Calendar.DAY_OF_YEAR, i);
                try {
                    currentDate = new SimpleDateFormat(pattern).format(calendar.getTime());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                dates.add(currentDate);
            }
        }
        return dates;
    }

    /**
     * 获得指定日期范围内所有的月份
     *
     * @param beginDate 起始日期，格式[yyyyMM]
     * @param endDate   截止日期，格式[yyyyMM]
     * @return 月份列表
     */
    public static List<String> listDatesBetweenMonth(String beginDate, String endDate) {
        List<String> dates = new ArrayList<String>();
        if (StringUtils.equals(beginDate, endDate)) {
            dates.add(beginDate);
        } else {
            Date beginDateDt = DateTimeUtils.toDate(beginDate, DateTimeUtils.y4M2_NON);
            Date endDateDt = DateTimeUtils.toDate(endDate, DateTimeUtils.y4M2_NON);
            ArrayList<String> result = new ArrayList<String>();
            Calendar min = Calendar.getInstance();
            min.setTime(beginDateDt);
            min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
            Calendar max = Calendar.getInstance();
            max.setTime(endDateDt);
            max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);
            Calendar curr = min;
            while (curr.before(max)) {
                result.add(new SimpleDateFormat(DateTimeUtils.y4M2_NON).format(curr.getTime()));
                curr.add(Calendar.MONTH, 1);
            }
            return result;
        }
        return dates;
    }

    /**
     * 获得指定日期范围内所有的年份
     *
     * @param beginDate 起始日期，格式[yyyy]
     * @param endDate   截止日期，格式[yyyy]
     * @return 年份列表
     */
    public static List<String> listDatesBetweenYear(String beginDate, String endDate) {
        List<String> dates = new ArrayList<String>();
        if (StringUtils.equals(beginDate, endDate)) {
            dates.add(beginDate);
        } else {
            Date beginDateDt = DateTimeUtils.toDate(beginDate, DateTimeUtils.y4);
            Date endDateDt = DateTimeUtils.toDate(endDate, DateTimeUtils.y4);
            ArrayList<String> result = new ArrayList<String>();
            Calendar min = Calendar.getInstance();
            min.setTime(beginDateDt);
            min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
            Calendar max = Calendar.getInstance();
            max.setTime(endDateDt);
            max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);
            Calendar curr = min;
            while (curr.before(max)) {
                result.add(new SimpleDateFormat(DateTimeUtils.y4).format(curr.getTime()));
                curr.add(Calendar.YEAR, 1);
            }
            return result;
        }
        return dates;
    }

    /**
     * 获取当天00:00:00
     *
     * @return
     */
    public static Date getCurrentDayBegin() {
        return toDate(getCurrentDateTime(y4M2d2) + " 00:00:00", y4M2d2H2m2s2);
    }

    /**
     * 获取当天23:59:59
     *
     * @return
     */
    public static Date getCurrentDayEnd() {
        return toDate(getCurrentDateTime(y4M2d2) + " 23:59:59", y4M2d2H2m2s2);
    }

    /**
     * 获取当前秒和毫秒拼接而成的Long型对象
     */
    public static Long getCurrentMsec() {
        String currentMsec = DateTimeUtils.getCurrentDateTime(s2S3_NON);
        return Long.parseLong(currentMsec);
    }

    /**
     * 格式化字符串为Timestamp类型
     *
     * @param str
     * @param pattern
     * @return
     */
    public static Timestamp formatStringToTimestamp(String str, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setLenient(false); // 严格解析

        Timestamp timestamp = null;
        try {
            timestamp = new Timestamp(format.parse(str).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    /**
     * 获取当前周的日期
     *
     * @return 日期集合
     */
    public static List<String> listCurrentWeekDate(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        List<String> currentWeekDates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        // 今天是周几
        int currentSeq = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (currentSeq == 0) {
            currentSeq = 7;
        }
        // 定位到本周星期一
        cal.add(Calendar.DATE, 1 - currentSeq);
        int index = 1;
        do {
            currentWeekDates.add(format.format(cal.getTime()));
            cal.add(Calendar.DATE, 1);
            index++;
        } while (index <= 7);
        return currentWeekDates;
    }

    /**
     * 获取前几个月的开始和结束时间
     *
     * @param amount 当前月 0
     *               上一个月 1
     *               上上个月 2
     */
    public static List<String> getBeforeMonthDate(int amount) {
        //规定返回日期格式
        SimpleDateFormat format = new SimpleDateFormat(y4M2d2_NON);
        List<String> currentWeekDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date theDate = calendar.getTime();
        GregorianCalendar gcLast = (GregorianCalendar) Calendar.getInstance();
        gcLast.setTime(theDate);
        calendar.add(Calendar.MONTH, amount);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        currentWeekDates.add(format.format(calendar.getTime()) + "000000");
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        currentWeekDates.add(format.format(calendar.getTime()) + "235959");
        return currentWeekDates;
        //打印本月第一天
    }

    /**
     * 获取某给日期的往前/往后N天的时间 精确到天
     *
     * @param date
     * @param day(-+)
     * @return
     */
    public static Date getIntervalDayDate(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //往前/后推N天(-+)
        calendar.add(Calendar.DATE, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date tempDate = calendar.getTime();
        return tempDate;
    }

    /**
     * 获取某给日期的往前/往后N天的时间 精确到毫秒
     *
     * @param date
     * @param day
     * @return
     */
    public static Date getIntervalDayDateTime(Date date, long day) {
        return formatDateTime(new Date(date.getTime() + day * 24 * 60 * 60 * 1000), y4M2d2H2m2s2);
    }


    /**
     * 获取当前月的第一天0时0秒
     * yyyy-mm-dd 0:00:00
     *
     * @return
     */
    public static Date getFirstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date tempDate = calendar.getTime();
        return tempDate;
    }

    /**
     * 获取指定日期前后N天的月日
     *
     * @param day     +-
     * @param dateStr
     * @param pattern
     * @return
     */
    public static String getMonthAndDay(String dateStr, String pattern, Integer day) {
        Date tempDate = DateTimeUtils.toDate(dateStr, pattern);
        tempDate = getIntervalDayDate(tempDate, day);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        calendar.setTime(tempDate);
        return String.format("%s月%s日", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static String currentDatetime() {
        return formatDate(new Date());
    }

    public static Timestamp parseDate(String dateStr, String pattern) throws ParseException {
        Date d = DateTimeUtils.parse(dateStr, pattern);
        return new Timestamp(d.getTime());
    }

    public static Timestamp parseDate(String dateStr) throws ParseException {
        return parseDate(dateStr, y4M2d2);
    }

    public static java.sql.Date parseSQLDate(String dateStr, String pattern) throws ParseException {
        Date d = parse(dateStr, pattern);
        return new java.sql.Date(d.getTime());
    }

    public static java.sql.Date parseSQLDate(String dateStr) throws ParseException {
        Date d = parse(dateStr, y4M2d2);
        return new java.sql.Date(d.getTime());
    }

    public static Timestamp getFutureTime(int month) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, month);
        return new Timestamp(c.getTimeInMillis());
    }

    /**
     * 显示今天时间
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String today() {
        return formatDateTime(new Date());
    }

    public static String formatDate(Timestamp t) {
        return formatDate(new Date(t.getTime()));
    }

    public static String formatDate(Timestamp t, String pattern) {
        return formatDate(new Date(t.getTime()), y4M2d2);
    }

    public static String formatDateTime(Timestamp t, String pattern) {
        return formatDate(new Date(t.getTime()), y4M2d2H2m2s2);
    }

    /**
     * 格式化日期
     *
     * @param date
     * @return yyyy-MM-dd
     */
    public static String formatDate(Date date) {
        return formatDate(date, y4M2d2);
    }

    /**
     * 格式化日期
     *
     * @param date
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, y4M2d2H2m2s2);
    }

    /**
     * 格式化日期
     *
     * @param date
     * @return pattern
     */
    public static Date formatDateTime(Date date, String pattern) {
        return toDate(formatDate(date, pattern), pattern);
    }

    /**
     * 格式化日期
     *
     * @param date    日期
     * @param pattern 格式
     * @return
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        DateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * 解析日期
     *
     * @param dateStr yyyy-MM-dd
     * @return
     */
    public static Date parse(String dateStr) {
        return parse(dateStr, y4M2d2);
    }

    /**
     * 解析日期
     *
     * @param dateStr yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Date parseTime(String dateStr) {
        return parse(dateStr, y4M2d2H2m2s2);
    }

    public static Date parse(String dateStr, String pattern) {

        try {
            DateFormat format = new SimpleDateFormat(pattern);
            return format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 当月的第一天
     *
     * @return
     */
    public static String firstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return DateTimeUtils.formatDate(calendar.getTime()) + " 00:00:00";
    }

    /**
     * 当月的最后一天
     *
     * @return
     */
    public static String lastDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        return DateTimeUtils.formatDate(calendar.getTime()) + " 23:59:59";
    }

    /**
     * 获取某月的最后一天
     */
    public static String getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR, year);
        //设置月份
        cal.set(Calendar.MONTH, month - 1);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
        return lastDayOfMonth;
    }

    /**
     * 获取当前时间
     * 年月日时分秒，格式：yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static Date getNowDateTime() {
        return toDate(getCurrentDateTime(), DateTimeUtils.y4M2d2H2m2s2);
    }

    /**
     * 获取当前时间
     *
     * @param pattern 格式
     * @return
     */
    public static Date getNowDateTime(String pattern) {
        return toDate(getCurrentDateTime(pattern), pattern);
    }

    /**
     * 时间戳转日期
     *
     * @param t       时间戳
     * @param pattern 格式
     * @return
     */
    public static Date timestampToDate(Timestamp t, String pattern) {
        return toDate(formatDate(new Date(t.getTime()), pattern), pattern);
    }

    /**
     * 时间戳转日期
     *
     * @param millis  毫秒值
     * @param pattern 格式
     * @return
     */
    public static Date timestampToDate(Long millis, String pattern) {
        return toDate(formatDate(new Date(millis), pattern), pattern);
    }

    /**
     * 时间戳转日期字符串
     *
     * @param t       时间戳
     * @param pattern 格式
     * @return
     */
    public static String timestampToDateStr(Timestamp t, String pattern) {
        return formatDate(new Date(t.getTime()), pattern);
    }

    /**
     * 时间戳转日期字符串
     *
     * @param millis  时间戳
     * @param pattern 格式
     * @return
     */
    public static String timestampToDateStr(Long millis, String pattern) {
        return formatDate(new Date(millis), pattern);
    }

    /**
     * 获取今天的最后时刻
     *
     * @return
     */
    public static Date getTodayEndTime() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 获取今天的最后时刻
     *
     * @return
     */
    public static String getTodayEndTimeStr() {
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取今天的开始时刻
     *
     * @return
     */
    public static String getTodayStartTimeStr() {
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取n个小时前的时间
     * @param date
     * @param n
     * @param pattern
     * @return n小时前的整点 0分0秒
     */
    public static String getLastHourTime(Date date, int n, String pattern) {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY) - n);
        date = ca.getTime();
        return sdf.format(date);
    }
}
