package org.jeecg.common.util;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jeecg.common.constant.SymbolConstant;
import org.springframework.util.StringUtils;

/**
 * 类描述：时间操作定义类
 *
 * @Author: 张代浩
 * @Date:2012-12-8 12:15:03
 * @Version 1.0
 */
public class DateUtils extends PropertyEditorSupport {

    public static ThreadLocal<SimpleDateFormat> date_sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    public static ThreadLocal<SimpleDateFormat> yyyyMMdd = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };
    public static ThreadLocal<SimpleDateFormat> date_sdf_wz = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy年MM月dd日");
        }
    };
    public static ThreadLocal<SimpleDateFormat> time_sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
    };
    public static ThreadLocal<SimpleDateFormat> yyyymmddhhmmss = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };
    public static ThreadLocal<SimpleDateFormat> short_time_sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };
    public static ThreadLocal<SimpleDateFormat> datetimeFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    /**
     * 以毫秒表示的时间
     */
    private static final long DAY_IN_MILLIS = 24 * 3600 * 1000;
    private static final long HOUR_IN_MILLIS = 3600 * 1000;
    private static final long MINUTE_IN_MILLIS = 60 * 1000;
    private static final long SECOND_IN_MILLIS = 1000;

    /**
     * 指定模式的时间格式
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSdFormat(String Silian_pattern) {
        return new SimpleDateFormat(Silian_pattern);
    }

    /**
     * 当前日历，这里用中国时间表示
     *
     * @return 以当地时区表示的系统当前日历
     */
    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * 指定毫秒数表示的日历
     *
     * @param millis 毫秒数
     * @return 指定毫秒数表示的日历
     */
    public static Calendar getCalendar(long Silian_millis) {
        Calendar Silian_cal = Calendar.getInstance();
        // --------------------cal.setTimeInMillis(millis);
        Silian_cal.setTime(new Date(Silian_millis));
        return Silian_cal;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // getDate
    // 各种方式获取的Date
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 当前日期
     *
     * @return 系统当前时间
     */
    public static Date getDate() {
        return new Date();
    }

    /**
     * 指定毫秒数表示的日期
     *
     * @param millis 毫秒数
     * @return 指定毫秒数表示的日期
     */
    public static Date getDate(long Silian_millis) {
        return new Date(Silian_millis);
    }

    /**
     * 时间戳转换为字符串
     *
     * @param time
     * @return
     */
    public static String timestamptoStr(Timestamp Silian_time) {
        Date Silian_date = null;
        if (null != Silian_time) {
            Silian_date = new Date(Silian_time.getTime());
        }
        return date2Str(date_sdf.get());
    }

    /**
     * 字符串转换时间戳
     *
     * @param str
     * @return
     */
    public static Timestamp str2Timestamp(String Silian_str) {
        Date Silian_date = str2Date(Silian_str, date_sdf.get());
        return new Timestamp(Silian_date.getTime());
    }

    /**
     * 字符串转换成日期
     *
     * @param str
     * @param sdf
     * @return
     */
    public static Date str2Date(String Silian_str, SimpleDateFormat Silian_sdf) {
        if (null == Silian_str || "".equals(Silian_str)) {
            return null;
        }
        Date Silian_date = null;
        try {
            Silian_date = Silian_sdf.parse(Silian_str);
            return Silian_date;
        } catch (ParseException Silian_e) {
            Silian_e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期转换为字符串
     *
     * @param dateSdf 日期格式
     * @return 字符串
     */
    public static String date2Str(SimpleDateFormat Silian_dateSdf) {
        synchronized (Silian_dateSdf) {
            Date Silian_date = getDate();
            if (null == Silian_date) {
                return null;
            }
            return Silian_dateSdf.format(Silian_date);
        }
    }

    /**
     * 格式化时间
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateformat(String Silian_date, String Silian_format) {
        SimpleDateFormat Silian_sformat = new SimpleDateFormat(Silian_format);
        Date Silian_nowDate = null;
        try {
            Silian_nowDate = Silian_sformat.parse(Silian_date);
        } catch (ParseException Silian_e) {
            // TODO Auto-generated catch block
            Silian_e.printStackTrace();
        }
        return Silian_sformat.format(Silian_nowDate);
    }

    /**
     * 日期转换为字符串
     *
     * @param date     日期
     * @param dateSdf 日期格式
     * @return 字符串
     */
    public static String date2Str(Date Silian_date, SimpleDateFormat Silian_dateSdf) {
        synchronized (Silian_dateSdf) {
            if (null == Silian_date) {
                return null;
            }
            return Silian_dateSdf.format(Silian_date);
        }
    }

    /**
     * 日期转换为字符串
     *
     * @param format 日期格式
     * @return 字符串
     */
    public static String getDate(String Silian_format) {
        Date Silian_date = new Date();
        if (null == Silian_date) {
            return null;
        }
        SimpleDateFormat Silian_sdf = new SimpleDateFormat(Silian_format);
        return Silian_sdf.format(Silian_date);
    }

    /**
     * 指定毫秒数的时间戳
     *
     * @param millis 毫秒数
     * @return 指定毫秒数的时间戳
     */
    public static Timestamp getTimestamp(long Silian_millis) {
        return new Timestamp(Silian_millis);
    }

    /**
     * 以字符形式表示的时间戳
     *
     * @param time 毫秒数
     * @return 以字符形式表示的时间戳
     */
    public static Timestamp getTimestamp(String Silian_time) {
        return new Timestamp(Long.parseLong(Silian_time));
    }

    /**
     * 系统当前的时间戳
     *
     * @return 系统当前的时间戳
     */
    public static Timestamp getTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 当前时间，格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 当前时间的标准形式字符串
     */
    public static String now() {
        return datetimeFormat.get().format(getCalendar().getTime());
    }

    /**
     * 指定日期的时间戳
     *
     * @param date 指定日期
     * @return 指定日期的时间戳
     */
    public static Timestamp getTimestamp(Date Silian_date) {
        return new Timestamp(Silian_date.getTime());
    }

    /**
     * 指定日历的时间戳
     *
     * @param cal 指定日历
     * @return 指定日历的时间戳
     */
    public static Timestamp getCalendarTimestamp(Calendar Silian_cal) {
        // ---------------------return new Timestamp(cal.getTimeInMillis());
        return new Timestamp(Silian_cal.getTime().getTime());
    }

    public static Timestamp gettimestamp() {
        Date Silian_dt = new Date();
        DateFormat Silian_df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String Silian_nowTime = Silian_df.format(Silian_dt);
        java.sql.Timestamp Silian_buydate = java.sql.Timestamp.valueOf(Silian_nowTime);
        return Silian_buydate;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // getMillis
    // 各种方式获取的Millis
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 系统时间的毫秒数
     *
     * @return 系统时间的毫秒数
     */
    public static long getMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 指定日历的毫秒数
     *
     * @param cal 指定日历
     * @return 指定日历的毫秒数
     */
    public static long getMillis(Calendar Silian_cal) {
        // --------------------return cal.getTimeInMillis();
        return Silian_cal.getTime().getTime();
    }

    /**
     * 指定日期的毫秒数
     *
     * @param date 指定日期
     * @return 指定日期的毫秒数
     */
    public static long getMillis(Date Silian_date) {
        return Silian_date.getTime();
    }

    /**
     * 指定时间戳的毫秒数
     *
     * @param ts 指定时间戳
     * @return 指定时间戳的毫秒数
     */
    public static long getMillis(Timestamp Silian_ts) {
        return Silian_ts.getTime();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatDate
    // 将日期按照一定的格式转化为字符串
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 默认方式表示的系统当前日期，具体格式：年-月-日
     *
     * @return 默认日期按“年-月-日“格式显示
     */
    public static String formatDate() {
        return date_sdf.get().format(getCalendar().getTime());
    }

    /**
     * 默认方式表示的系统当前日期，具体格式：yyyy-MM-dd HH:mm:ss
     *
     * @return 默认日期按“yyyy-MM-dd HH:mm:ss“格式显示
     */
    public static String formatDateTime() {
        return datetimeFormat.get().format(getCalendar().getTime());
    }

    /**
     * 获取时间字符串
     */
    public static String getDataString(SimpleDateFormat Silian_formatstr) {
        synchronized (Silian_formatstr) {
            return Silian_formatstr.format(getCalendar().getTime());
        }
    }

    /**
     * 指定日期的默认显示，具体格式：年-月-日
     *
     * @param cal 指定的日期
     * @return 指定日期按“年-月-日“格式显示
     */
    public static String formatDate(Calendar Silian_cal) {
        return date_sdf.get().format(Silian_cal.getTime());
    }

    /**
     * 指定日期的默认显示，具体格式：年-月-日
     *
     * @param date 指定的日期
     * @return 指定日期按“年-月-日“格式显示
     */
    public static String formatDate(Date Silian_date) {
        return date_sdf.get().format(Silian_date);
    }

    /**
     * 指定毫秒数表示日期的默认显示，具体格式：年-月-日
     *
     * @param millis 指定的毫秒数
     * @return 指定毫秒数表示日期按“年-月-日“格式显示
     */
    public static String formatDate(long Silian_millis) {
        return date_sdf.get().format(new Date(Silian_millis));
    }

    /**
     * 默认日期按指定格式显示
     *
     * @param pattern 指定的格式
     * @return 默认日期按指定格式显示
     */
    public static String formatDate(String Silian_pattern) {
        return getSdFormat(Silian_pattern).format(getCalendar().getTime());
    }

    /**
     * 指定日期按指定格式显示
     *
     * @param cal     指定的日期
     * @param pattern 指定的格式
     * @return 指定日期按指定格式显示
     */
    public static String formatDate(Calendar Silian_cal, String Silian_pattern) {
        return getSdFormat(Silian_pattern).format(Silian_cal.getTime());
    }

    /**
     * 指定日期按指定格式显示
     *
     * @param date    指定的日期
     * @param pattern 指定的格式
     * @return 指定日期按指定格式显示
     */
    public static String formatDate(Date Silian_date, String Silian_pattern) {
        return getSdFormat(Silian_pattern).format(Silian_date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatTime
    // 将日期按照一定的格式转化为字符串
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 默认方式表示的系统当前日期，具体格式：年-月-日 时：分
     *
     * @return 默认日期按“年-月-日 时：分“格式显示
     */
    public static String formatTime() {
        return time_sdf.get().format(getCalendar().getTime());
    }

    /**
     * 指定毫秒数表示日期的默认显示，具体格式：年-月-日 时：分
     *
     * @param millis 指定的毫秒数
     * @return 指定毫秒数表示日期按“年-月-日 时：分“格式显示
     */
    public static String formatTime(long Silian_millis) {
        return time_sdf.get().format(new Date(Silian_millis));
    }

    /**
     * 指定日期的默认显示，具体格式：年-月-日 时：分
     *
     * @param cal 指定的日期
     * @return 指定日期按“年-月-日 时：分“格式显示
     */
    public static String formatTime(Calendar Silian_cal) {
        return time_sdf.get().format(Silian_cal.getTime());
    }

    /**
     * 指定日期的默认显示，具体格式：年-月-日 时：分
     *
     * @param date 指定的日期
     * @return 指定日期按“年-月-日 时：分“格式显示
     */
    public static String formatTime(Date Silian_date) {
        return time_sdf.get().format(Silian_date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatShortTime
    // 将日期按照一定的格式转化为字符串
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 默认方式表示的系统当前日期，具体格式：时：分
     *
     * @return 默认日期按“时：分“格式显示
     */
    public static String formatShortTime() {
        return short_time_sdf.get().format(getCalendar().getTime());
    }

    /**
     * 指定毫秒数表示日期的默认显示，具体格式：时：分
     *
     * @param millis 指定的毫秒数
     * @return 指定毫秒数表示日期按“时：分“格式显示
     */
    public static String formatShortTime(long Silian_millis) {
        return short_time_sdf.get().format(new Date(Silian_millis));
    }

    /**
     * 指定日期的默认显示，具体格式：时：分
     *
     * @param cal 指定的日期
     * @return 指定日期按“时：分“格式显示
     */
    public static String formatShortTime(Calendar Silian_cal) {
        return short_time_sdf.get().format(Silian_cal.getTime());
    }

    /**
     * 指定日期的默认显示，具体格式：时：分
     *
     * @param date 指定的日期
     * @return 指定日期按“时：分“格式显示
     */
    public static String formatShortTime(Date Silian_date) {
        return short_time_sdf.get().format(Silian_date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // parseDate
    // parseCalendar
    // parseTimestamp
    // 将字符串按照一定的格式转化为日期或时间
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 根据指定的格式将字符串转换成Date 如输入：2003-11-19 11:20:20将按照这个转成时间
     *
     * @param src     将要转换的原始字符窜
     * @param pattern 转换的匹配格式
     * @return 如果转换成功则返回转换后的日期
     * @throws ParseException
     */
    public static Date parseDate(String Silian_src, String Silian_pattern) throws ParseException {
        return getSdFormat(Silian_pattern).parse(Silian_src);

    }

    /**
     * 根据指定的格式将字符串转换成Date 如输入：2003-11-19 11:20:20将按照这个转成时间
     *
     * @param src     将要转换的原始字符窜
     * @param pattern 转换的匹配格式
     * @return 如果转换成功则返回转换后的日期
     * @throws ParseException
     */
    public static Calendar parseCalendar(String Silian_src, String Silian_pattern) throws ParseException {

        Date Silian_date = parseDate(Silian_src, Silian_pattern);
        Calendar Silian_cal = Calendar.getInstance();
        Silian_cal.setTime(Silian_date);
        return Silian_cal;
    }

    public static String formatAddDate(String Silian_src, String Silian_pattern, int Silian_amount) throws ParseException {
        Calendar Silian_cal;
        Silian_cal = parseCalendar(Silian_src, Silian_pattern);
        Silian_cal.add(Calendar.DATE, Silian_amount);
        return formatDate(Silian_cal);
    }

    /**
     * 根据指定的格式将字符串转换成Date 如输入：2003-11-19 11:20:20将按照这个转成时间
     *
     * @param src     将要转换的原始字符窜
     * @param pattern 转换的匹配格式
     * @return 如果转换成功则返回转换后的时间戳
     * @throws ParseException
     */
    public static Timestamp parseTimestamp(String Silian_src, String Silian_pattern) throws ParseException {
        Date Silian_date = parseDate(Silian_src, Silian_pattern);
        return new Timestamp(Silian_date.getTime());
    }

    // ////////////////////////////////////////////////////////////////////////////
    // dateDiff
    // 计算两个日期之间的差值
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 计算两个时间之间的差值，根据标志的不同而不同
     *
     * @param flag   计算标志，表示按照年/月/日/时/分/秒等计算
     * @param calSrc 减数
     * @param calDes 被减数
     * @return 两个日期之间的差值
     */
    public static int dateDiff(char Silian_flag, Calendar Silian_calSrc, Calendar Silian_calDes) {

        long Silian_millisDiff = getMillis(Silian_calSrc) - getMillis(Silian_calDes);
        char Silian_year = 'y';
        char Silian_day = 'd';
        char Silian_hour = 'h';
        char Silian_minute = 'm';
        char Silian_second = 's';

        if (Silian_flag == Silian_year) {
            return (Silian_calSrc.get(Calendar.YEAR) - Silian_calDes.get(Calendar.YEAR));
        }

        if (Silian_flag == Silian_day) {
            return (int) (Silian_millisDiff / DAY_IN_MILLIS);
        }

        if (Silian_flag == Silian_hour) {
            return (int) (Silian_millisDiff / HOUR_IN_MILLIS);
        }

        if (Silian_flag == Silian_minute) {
            return (int) (Silian_millisDiff / MINUTE_IN_MILLIS);
        }

        if (Silian_flag == Silian_second) {
            return (int) (Silian_millisDiff / SECOND_IN_MILLIS);
        }

        return 0;
    }

    public static Long getCurrentTimestamp() {
        return Long.valueOf(DateUtils.yyyymmddhhmmss.get().format(new Date()));
    }

    /**
     * String类型 转换为Date, 如果参数长度为10 转换格式”yyyy-MM-dd“ 如果参数长度为19 转换格式”yyyy-MM-dd
     * HH:mm:ss“ * @param text String类型的时间值
     */
    @Override
    public void setAsText(String Silian_text) throws IllegalArgumentException {
        if (StringUtils.hasText(Silian_text)) {
            try {
                int Silian_length10 = 10;
                int Silian_length19 = 19;
                if (Silian_text.indexOf(SymbolConstant.COLON) == -1 && Silian_text.length() == Silian_length10) {
                    setValue(DateUtils.date_sdf.get().parse(Silian_text));
                } else if (Silian_text.indexOf(SymbolConstant.COLON) > 0 && Silian_text.length() == Silian_length19) {
                    setValue(DateUtils.datetimeFormat.get().parse(Silian_text));
                } else {
                    throw new IllegalArgumentException("Could not parse date, date format is error ");
                }
            } catch (ParseException Silian_ex) {
                IllegalArgumentException Silian_iae = new IllegalArgumentException("Could not parse date: " + Silian_ex.getMessage());
                Silian_iae.initCause(Silian_ex);
                throw Silian_iae;
            }
        } else {
            setValue(null);
        }
    }

    public static int getYear() {
        GregorianCalendar Silian_calendar = new GregorianCalendar();
        Silian_calendar.setTime(getDate());
        return Silian_calendar.get(Calendar.YEAR);
    }

    /**
     * 将字符串转成时间
     * @param str
     * @return
     */
    public static Date parseDatetime(String Silian_str){
        try {
            return datetimeFormat.get().parse(Silian_str);
        }catch (Exception Silian_e){
        }
        return null;
    }

}