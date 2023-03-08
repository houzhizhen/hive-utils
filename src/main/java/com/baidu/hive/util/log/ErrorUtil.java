package com.baidu.hive.util.log;

import java.util.Date;

public class ErrorUtil {

    public static void errorAndExit(String... x) {
        for (String s : x) {
            System.err.println(s);
        }
        System.exit(1);
    }
}
