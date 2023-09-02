package com.baidu.hive.func;

import org.apache.hadoop.hive.common.type.Date;
import org.apache.hadoop.hive.common.type.Timestamp;
import org.junit.Test;

import java.time.ZoneOffset;
import java.util.TimeZone;

public class TestDate {

    @Test
    public void test15Hour() {
        java.sql.Date sqlDate = new java.sql.Date(15 * 3600 * 1000L);
        System.out.println("sqlDate.getTime():" + sqlDate.getTime());
        System.out.println("sqlDate:" + sqlDate.toString());
        Date javaDate = Date.ofEpochMilli(sqlDate.getTime());
        System.out.println("javaDate:" + javaDate.toEpochDay());
    }

    @Test
    public void test16Hour() {
        java.sql.Date sqlDate = new java.sql.Date(16 * 3600 * 1000L);
        System.out.println("sqlDate.getTime():" + sqlDate.getTime());
        System.out.println("sqlDate:" + sqlDate.toString());
        Date javaDate = Date.ofEpochMilli(sqlDate.getTime());
        System.out.println("javaDate:" + javaDate.toEpochDay());
    }
    public static void main(String[] args) {

        TimeZone gmt = TimeZone.getTimeZone("GMT");
        System.out.println("gmt.getRawOffset():" + gmt.getRawOffset());
        System.out.println(TimeZone.getTimeZone(ZoneOffset.systemDefault().getId()).getRawOffset());
        System.out.println("Timestamp test:");
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(16 * 3600 * 1000L);
        System.out.println("sqlTimestamp.getTime():" + sqlTimestamp.getTime());
        System.out.println("sqlTimestamp:" + sqlTimestamp.toString());
        Timestamp hiveTimestamp =Timestamp.ofEpochMilli(sqlTimestamp.getTime());
        System.out.println("hiveTimestamp:" + hiveTimestamp.toString());

        System.out.println("Date test:");
        java.sql.Date sqlDate = new java.sql.Date(16 * 3600 * 1000L);
        System.out.println("sqlDate.getTime():" + sqlDate.getTime());
        System.out.println("sqlDate:" + sqlDate.toString());
        Date javaDate = Date.ofEpochMilli(sqlDate.getTime());
        System.out.println("javaDate:" + javaDate.toString());


        Date javaDate2 = Date.of(1970,1,1);
        System.out.println("javaDate2:" + javaDate2.toString());
    }
}
