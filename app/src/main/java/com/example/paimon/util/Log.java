package com.example.paimon.util;

import java.util.HashMap;
import java.util.Map;

public class Log {

    private static String clazz;

    private static String method;

    private static int line;

    private static String location;

    private static Map<String, String> info = new HashMap<>();

    //获取打印信息所在方法名，行号等信息
    static {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length < 5) {
            android.util.Log.e(StringUtil.TAG, "Stack is too shallow!!!");
        } else {
            clazz = elements[4].getClassName().substring(elements[4].getClassName().lastIndexOf(".") + 1);
            method = elements[4].getMethodName();
            line = elements[4].getLineNumber();
            location = clazz + "#" + line;
        }
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(tag, location + "----" + msg);
    }

    public static void d(String tag, String msg) {
        android.util.Log.d(tag, location + "----" + msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(tag, location + "----" + msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, location + "----" + msg);
    }
    public static void e(Throwable e) {
        android.util.Log.e(StringUtil.TAG, e.getMessage(), e);
    }


    public static void e(String tag, String msg, Throwable e) {
        android.util.Log.e(tag, location + "----" + msg, e);
    }

    public static void d(String msg) {
        d(StringUtil.TAG, location + "----" + msg);
    }

}
