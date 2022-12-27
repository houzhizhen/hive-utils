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

public class ParallelStatementTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);
        String defaultHiveUrl = "jdbc:hive2://localhost:10000/default";


        String hiveUrl = hiveConf.get("hiveUrl", defaultHiveUrl);
        int times = hiveConf.getInt("times", 10);
        String sql = hiveConf.get("sql", "select 1");
        int parallelism = hiveConf.getInt("parallelism", 2);

        String userName = hiveConf.get("userName", "hive");
        LogUtil.log("Parameters:");
        LogUtil.log("hiveUrl = " + hiveUrl);
        LogUtil.log("times = " + times);
        LogUtil.log("sql = " + sql);
        LogUtil.log("userName = " + userName);
        Class.forName(HiveDriver.class.getName());
        AtomicBoolean error = new AtomicBoolean(false);
        try {
            Connection conn = java.sql.DriverManager.getConnection(hiveUrl, userName, "hive");

            parallelExecute(conn, sql, parallelism, times);

            conn.close();
        } catch(Throwable t) {
            error.set(true);
            t.printStackTrace();
        } finally {

            if (error.get()) {
                LogUtil.log("Error Finished");
            } else {
                LogUtil.log("Success Finished");
                // TimeUnit.SECONDS.sleep(3600);
            }
        }
    }

    public static void parallelExecute(Connection conn, String sql, int parallelism, int times) throws InterruptedException {
        CountDownLatch countDown = new CountDownLatch(parallelism);
        ExecutorService es = Executors.newFixedThreadPool(parallelism);
        for (int i = 0; i < parallelism; i++) {
            es.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("start countdown");
                        countDown.countDown();
                        System.out.println("end countdown");
                        Statement st = conn.createStatement();
                        for (int j = 0; j < times; j++) {
                            st.executeQuery(sql);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        es.shutdownNow();
        es.awaitTermination(1, TimeUnit.HOURS);
    }
}
