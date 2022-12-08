package com.baidu.hive.util.rand;

import com.baidu.hive.util.log.LogUtil;

import java.util.Random;

public class RandomUtil {

    private static final Random random = new Random();
    private static final String ALL_CARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (long i = 0; i < length; i++) {
            sb.append(ALL_CARACTERS.charAt(random.nextInt(ALL_CARACTERS.length())));
        }
        return sb.toString();
    }

    public static String randomDecimal(int precision, int scale) {
        // if scale is 38 and precision is 0, only generate 5 digits.
        int maxScale = scale - precision;
        if (maxScale > 5) {
            maxScale = 5;
        }
        int limit = (int)Math.pow(10, maxScale);
        return random.nextInt(limit) + "";
    }

    public static void main(String[] args) {
        LogUtil.log(randomDecimal(1, 0));
        LogUtil.log(randomDecimal(5, 2));
        LogUtil.log(randomDecimal(38, 0));
    }

    public static String randomDouble() {
        return random.nextDouble() + "";
    }
}
