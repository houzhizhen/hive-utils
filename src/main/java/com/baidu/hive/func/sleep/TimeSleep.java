package com.baidu.hive.func.sleep;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.concurrent.TimeUnit;

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
