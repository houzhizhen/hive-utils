package com.baidu.hive.util.log;

import java.text.SimpleDateFormat;

public class LogUtil {
    private static SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    public static void log(String... x) {
        for (String s : x) {
            System.out.println(s);
        }
    }
}
