package com.baidu.hive.func;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.UDF;

public class Echo extends UDF {

    private final static   Log LOG = LogFactory.getLog(Echo.class.getName());

    public int evaluate(int a) {
        LOG.info(String.valueOf(a));
        return a;
    }

    public long evaluate(long a) {
        return a;
    }

    public float evaluate(float a) {
        return a;
    }

    public double evaluate(double a) {
        return a;
    }

    public Object evalueate(Object o) {
        return o;
    }
}
