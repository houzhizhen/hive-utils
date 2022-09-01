package com.baidu.hive.util.log;

import org.apache.hadoop.hive.conf.HiveConf;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    private static SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    public static void log(String... x) {
        for (String s : x) {
            System.out.println(format.format(new Date()) + " " + s);
        }
    }

    public static void logParameter(HiveConf hiveConf, String parameterName) {
        log(parameterName + "=" + hiveConf.get(parameterName));
    }
}
