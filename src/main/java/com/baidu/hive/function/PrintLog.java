package com.baidu.hive.function;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintLog extends UDF {
    protected static final Logger LOG = LoggerFactory.getLogger(PrintLog.class.getName());
    public String evaluate(String str, long time) {
        for (int i = 0; i < time; i++) {
            LOG.info("str:" + str);
        }
        return "ok";
    }
}
