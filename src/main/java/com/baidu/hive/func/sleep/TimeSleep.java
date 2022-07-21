package com.baidu.hive.func.sleep;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.concurrent.TimeUnit;

/**
 * create function timesleep as 'com.baidu.hive.func.sleep.TimeSleep' using jar 'hdfs://localhost:9000/apps/hive/hive-util-0.1.0.jar';
 */
public class TimeSleep extends UDF {

    public long evaluate(long time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return time + 1;
    }
}
