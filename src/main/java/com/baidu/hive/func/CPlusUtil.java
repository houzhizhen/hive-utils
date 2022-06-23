package com.baidu.hive.func;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CPlusUtil {

    static {
        System.loadLibrary("cplus");
    }

    public static native int add(int a, int b);
}
