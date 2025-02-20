package com.baidu.hive.driver;

import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.common.io.CachingPrintStream;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.Context;
import org.apache.hadoop.hive.ql.lockmgr.LockException;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.hadoop.hive.ql.wm.WmContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class DriverBase {

    protected HiveConf conf;
    protected Context ctx = null;
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

        try {
            ctx = new Context(this.conf);
            WmContext wmContext = new WmContext(System.currentTimeMillis(), "1");
            ctx.setWmContext(wmContext);
            ctx.setHiveTxnManager(SessionState.get().initTxnMgr(conf));
            ctx.setStatsSource(null);

            ctx.setHDFSCleanup(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (LockException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeSession() {
        try {
            SessionState.get().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Context getContext() {
        return ctx;
    }
}
