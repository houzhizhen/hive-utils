package com.baidu.hive.driver.check;

import org.apache.hadoop.hive.conf.HiveConf;
import org.junit.Test;

public class TestCheckSqlInPath {

    @Test
    public void testSet() {
        HiveConf conf = new HiveConf();
        CheckSqlInPath checkSqlInPath = new CheckSqlInPath(conf);
        checkSqlInPath.createSession();
        checkSqlInPath.closeSession();
    }

    @Test
    public void testRepalaceVariable() {
        String cmd = "select *\n" +
                "from dp_dwddb${hiveconf:env_flag}.dwd_md_tsql_master_dqc_chain\n" +
                "where  dt='2021-06-22' and at > ${hivevar:yesterday}  and ss<${hivevar:lastweek}";
        cmd = CheckSqlInPath.replaceVariable(cmd, "1");
        System.out.println(cmd);
    }

    @Test
    public void testException() {
        HiveConf conf = new HiveConf();
        CheckSqlInPath checkSqlInPath = new CheckSqlInPath(conf);
        checkSqlInPath.createSession();
        checkSqlInPath.closeSession();
    }
}
