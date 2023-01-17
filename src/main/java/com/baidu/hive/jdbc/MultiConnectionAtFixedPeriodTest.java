package com.baidu.hive.jdbc;

import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hive.jdbc.HiveDriver;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MultiConnectionAtFixedPeriodTest {

    private static final String HIVE_URL_DEFAULT = "jdbc:hive2://localhost:10000/default";
    private static final int INTERVAL_SECONDS_DEFAULT = 1;
    private static final String SQL_DEFAULT = "select 1";
    private static final int PARALLELISM_DEFAULT = 2;
    private static final int TIMES_DEFAULT = 10;
    private static final String USERNAME_DEFAULT = "hive";
    private static final AtomicLong totalTimeInMillions = new AtomicLong();
    private static final AtomicLong statementExecuted = new AtomicLong(0);

    private static String hiveUrl;
    private static String sql;
    private static String userName;

    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
        HiveConf conf = new HiveConf();
        HiveTestUtils.addResource(conf, args);

        int parallelism = conf.getInt("parallelism", PARALLELISM_DEFAULT);
        long interval = conf.getLong("intervalSeconds", INTERVAL_SECONDS_DEFAULT);
        hiveUrl = conf.get("hiveUrl", HIVE_URL_DEFAULT);
        sql = conf.get("sql", SQL_DEFAULT);
        userName = conf.get("userName", USERNAME_DEFAULT);
        Class.forName(HiveDriver.class.getName());

        ScheduledExecutorService es = Executors.newScheduledThreadPool(100);
        es.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    parallelExecute(conf, parallelism);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 0L, interval, TimeUnit.SECONDS);

        ScheduledExecutorService es2 = Executors.newScheduledThreadPool(1);
        es2.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long executed =  statementExecuted.get();
                if (executed == 0) {
                    LogUtil.log("avg time: executed = 0");
                } else {
                    LogUtil.log("avg time:" + (totalTimeInMillions.get() / executed) +
                                        ", executed=" + executed);
                }

            }
        }, 0L, interval, TimeUnit.SECONDS);

        TimeUnit.HOURS.sleep(1);
    }


    public static void parallelExecute(HiveConf conf, int parallelism) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(parallelism);
        for (int i = 0; i < parallelism; i++) {
            es.submit(() -> {
                try {
                    execute(conf);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
        es.shutdownNow();
    }

    public static void execute(HiveConf conf) {
        long beginTime = System.currentTimeMillis();
        Connection conn = null;
        try {
            conn = java.sql.DriverManager.getConnection(hiveUrl, userName, "hive");
            Statement st = conn.createStatement();
            st.executeQuery(sql);
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            //error.set(true);
        } finally {
            long time = System.currentTimeMillis() - beginTime;
            totalTimeInMillions.addAndGet(time);
            statementExecuted.incrementAndGet();
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
