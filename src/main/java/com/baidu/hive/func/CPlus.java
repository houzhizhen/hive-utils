package com.baidu.hive.func;

import org.apache.hadoop.hive.ql.exec.UDF;

public class CPlus extends UDF {

    public int evaluate(int a, int b) {
        return CPlusUtil.add(a, b);
    }
}
