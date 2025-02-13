package com.baidu.hive.common.type;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static java.time.temporal.ChronoField.MILLI_OF_SECOND;

public class TestTimestamp {

    public static void main(String[] args) {
        LocalDateTime localDateTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        long time1 = localDateTime.atZone(ZoneOffset
                                                  .UTC).withZoneSameInstant(ZoneOffset.systemDefault()).getLong(MILLI_OF_SECOND);
        print("time1 = " + time1);
        long time2 = localDateTime.getLong(MILLI_OF_SECOND);
        print("time2 = " + time2);
        print(ZoneId.systemDefault().toString());

    }
    public static void print(String s) {
        System.out.println(s);
    }
}
