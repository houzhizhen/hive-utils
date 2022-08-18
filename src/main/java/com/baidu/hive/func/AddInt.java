package com.baidu.hive.func;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * Create function script:
 * create function add_int as 'com.baidu.hive.func.AddInt' using jar 'hdfs://localhost:9000/apps/hive/hive-util-0.1.0.jar';
 */
public class AddInt extends UDF {
    public int evaluate(int a) {
        return a + 1;
    }
}
