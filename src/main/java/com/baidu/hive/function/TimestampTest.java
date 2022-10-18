package com.baidu.hive.function;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;

public class TimestampTest {

    public static void main(String[] args) {
        System.out.println(ZoneId.systemDefault());
        System.out.println(OffsetDateTime.now().getOffset());
        ZoneOffset currentOffsetForMyZone = ZoneId.systemDefault().getRules().getOffset(Instant.now());
        System.out.println(currentOffsetForMyZone);
    }
}
