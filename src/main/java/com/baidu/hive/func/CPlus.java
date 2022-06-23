package com.baidu.hive.func;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.UDF;

public class CPlus extends UDF {

    private final static Log LOG = LogFactory.getLog(CPlus.class.getName());

    public int evaluate(int a, int b) {
        LOG.info("a = " + a + ", b = " + b);
        return CPlusUtil.add(a, b);
    }
}
