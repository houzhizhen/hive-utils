package com.baidu.hive.func;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * Create function script:
 * create function add_Long as 'com.baidu.hive.func.AddLong' using jar 'hdfs://localhost:9000/apps/hive/hive-util-0.1.0.jar';
 */
public class AddLong extends UDF {

    public long evaluate(long a) {
        return a + 1L;
    }
}
