package com.baidu.hive.func;

public class CPlusUtil {

    static {
        System.loadLibrary("cplus");
    }

    public static native int add(int a, int b);
}
