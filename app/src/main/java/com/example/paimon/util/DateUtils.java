package com.example.paimon.util;


import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {

    /**
     * 时间万能解析工具，年份必须是四位，月、日、时、分、秒可以是一位或两位，日期分隔符可以是任意非数字字符或没有分隔符，时间分隔符可以是任意非数字
     */
    public static Date universalParseDate(String dateStr) {
        if (StringUtil.isBlank(dateStr)) {
            return null;
        }
        String lastStr = dateStr.substring(dateStr.length() - 1);
        boolean isLastCharNumeric = lastStr.matches("\\d+");
        Pattern p = Pattern.compile("^\\d{4,8}(\\s+|$)");
        Matcher m = p.matcher(dateStr);
        boolean isDateLastCharNumeric = m.find();
        String limiter = isDateLastCharNumeric ? "(\\D?)" : "(\\D+)";
        String endSymbol = isLastCharNumeric ? "($)" : "(\\D+$)";
        String regex = "^(\\d{4})" + limiter + "((1[012])|([0]?[1-9]))" + limiter +
                "(([123][\\d]{1})|([0]?[\\d]{1}))(\\D+)((1\\d{1})|(2[0-4]{1})|(0?\\d{1}))(\\D+)(([1-5][\\d]{1})|([0]?[\\d]{1}))(\\D+)(([1-5][\\d]{1})|([0]?[\\d]{1}))" +
                endSymbol;
        while (true) {
            Pattern tablePattern = Pattern.compile(regex);
            Matcher tableMatcher = tablePattern.matcher(dateStr);
            if (tableMatcher.find()) {
                int year = Integer.parseInt(tableMatcher.group(1));
                int month = 1;
                int day = 1;
                int hour = 0;
                int min = 0;
                int sec = 0;
                int groupCount = tableMatcher.groupCount();
                if (groupCount >= 3) {
                    String monthStr = tableMatcher.group(3);
                    if (!StringUtil.isBlank(monthStr)) {
                        month = Integer.parseInt(monthStr);
                    }
                }
                if (groupCount >= 7) {
                    String dayStr = tableMatcher.group(7);
                    if (!StringUtil.isBlank(dayStr)) {
                        day = Integer.parseInt(dayStr);
                    }
                }
                if (groupCount >= 11) {
                    String hourStr = tableMatcher.group(11);
                    if (!StringUtil.isBlank(hourStr)) {
                        hour = Integer.parseInt(hourStr);
                    }
                }
                if (groupCount >= 16) {
                    String minStr = tableMatcher.group(16);
                    if (!StringUtil.isBlank(minStr)) {
                        min = Integer.parseInt(minStr);
                    }
                }
                if (groupCount >= 20) {
                    String secStr = tableMatcher.group(20);
                    if (!StringUtil.isBlank(secStr)) {
                        sec = Integer.parseInt(secStr);
                    }
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, day, hour, min, sec);
                if (calendar.get(Calendar.MONTH) + 1 != month) {
                    return null;
                }
                return calendar.getTime();
            } else {
                if (isLastCharNumeric) {
                    regex = regex.replace("($)", "");
                    int index = regex.lastIndexOf("(\\D");
                    if (index == -1) {
                        return null;
                    }
                    regex = regex.substring(0, index) + "($)";
                } else {
                    regex = regex.replace("(\\D+$)", "");
                    int index = regex.lastIndexOf("(\\D");
                    if (index == -1) {
                        return null;
                    }
                    regex = regex.substring(0, index) + "(\\D+$)";
                }
            }
        }
    }
}
