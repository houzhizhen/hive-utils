package com.baidu.hive.driver;

import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.common.io.CachingPrintStream;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class DriverBase {

    protected HiveConf conf;
    public DriverBase(HiveConf conf) {
        this.conf = conf;
    }

    public void createSession() {
        conf.set("hive.execution.engine", "mr");

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
