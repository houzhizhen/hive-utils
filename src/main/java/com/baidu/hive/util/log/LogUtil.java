package com.baidu.hive.util.log;

public class LogUtil {

    public static void log(String... x) {
        for (String s : x) {
            System.out.println(s);
        }
    }
}
