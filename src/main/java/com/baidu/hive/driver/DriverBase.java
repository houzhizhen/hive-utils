package com.baidu.hive.driver;

import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.common.io.CachingPrintStream;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class DriverBase {

    public void createSession() {
        HiveConf conf = new HiveConf(SessionState.class);
        conf.set("hive.execution.engine", "mr");
        for (Map.Entry<String, String> entry : conf) {
            LogUtil.log("key:" + entry.getKey() + ", value:" + entry.getValue());
        }
        CliSessionState ss = new CliSessionState(conf);
        ss.in = System.in;
        try {
            ss.out = new PrintStream(System.out, true, "UTF-8");
            ss.info = new PrintStream(System.err, true, "UTF-8");
            ss.err = new CachingPrintStream(System.err, true, "UTF-8");
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        CliSessionState.start(ss);
    }

    public void closeSession() {
        try {
            SessionState.get().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
