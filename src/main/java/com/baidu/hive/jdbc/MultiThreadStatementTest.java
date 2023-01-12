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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiThreadStatementTest {

    private static final String HIVE_URL_DEFAULT = "jdbc:hive2://localhost:10000/default";
    private static final int TIMES_DEFAULT = 10;
    private static final String SQL_DEFAULT = "select 1";
    private static final int PARALLELISM_DEFAULT = 10;
    private static final String USERNAME_DEFAULT = "hive";

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        HiveConf conf = new HiveConf();
        HiveTestUtils.addResource(conf, args);

        int parallelism = conf.getInt("parallelism", PARALLELISM_DEFAULT);

        LogUtil.log("Parameters:");
        LogUtil.log("parallelism = " + parallelism);

        Class.forName(HiveDriver.class.getName());
        AtomicBoolean error = new AtomicBoolean(false);
        try {
            parallelExecute(conf, parallelism, error);
        } finally {
            if (error.get()) {
                LogUtil.log("Error Finished");
            } else {
                LogUtil.log("Success Finished");
                // TimeUnit.SECONDS.sleep(3600);
            }
        }
    }

    public static void parallelExecute(HiveConf conf, int parallelism, AtomicBoolean error) throws InterruptedException {
        CountDownLatch countDown = new CountDownLatch(parallelism);
        ExecutorService es = Executors.newFixedThreadPool(parallelism);
        for (int i = 0; i < parallelism; i++) {
            es.submit(() -> {
                try {
                    System.out.println("start countdown");

                    execute(conf, error, countDown);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
        es.shutdownNow();
        boolean terminated = es.awaitTermination(1, TimeUnit.HOURS);
        LogUtil.log("terminated:" + terminated);
    }

    public static void execute(HiveConf conf, AtomicBoolean error, CountDownLatch countDown) {
        String hiveUrl = conf.get("hiveUrl", HIVE_URL_DEFAULT);
        int times = conf.getInt("times", TIMES_DEFAULT);
        String sql = conf.get("sql", SQL_DEFAULT);
        String userName = conf.get("userName", USERNAME_DEFAULT);
        LogUtil.log("hiveUrl = " + hiveUrl);
        LogUtil.log("times = " + times);
        LogUtil.log("sql = " + sql);
        LogUtil.log("userName = " + userName);
        Connection conn = null;
        try {
            conn = java.sql.DriverManager.getConnection(hiveUrl, userName, "hive");
            countDown.countDown();
            LogUtil.log("countDown.getCount():" + countDown.getCount());
            Statement st = conn.createStatement();
            for (int j = 0; j < times && !error.get(); j++) {
                st.executeQuery(sql);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            error.set(true);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("end countdown");
    }
}
