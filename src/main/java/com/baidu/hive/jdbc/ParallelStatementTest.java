package com.baidu.hive.jdbc;

import com.baidu.hive.util.log.LogUtil;
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
        String hiveUrl = "jdbc:hive2://localhost:10000/default";
        int times = 1000;
        String sql = "select echo(1)";
        String userName = "hive";
        boolean closeConnection = true;
        if (args.length >0) {
            hiveUrl = args[0];
        }
        if (args.length > 1) {
            times = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            sql = args[2];
        }
        if (args.length > 3) {
            closeConnection = Boolean.parseBoolean(args[3]);
        }
        if (args.length > 4) {
            userName =args[4];
        }
        LogUtil.log("Parameters:");
        LogUtil.log("hiveUrl = " + hiveUrl);
        LogUtil.log("times = " + times);
        LogUtil.log("sql = " + sql);
        LogUtil.log("closeConnection = " + closeConnection);
        LogUtil.log("userName = " + userName);
        Class.forName(HiveDriver.class.getName());
        AtomicBoolean error = new AtomicBoolean(false);
        try {
            for (int i = 0; i < times; i++) {
                LogUtil.log("i = " + i);
                Connection conn = java.sql.DriverManager.getConnection(hiveUrl, userName, "hive");
                parallelExecute(conn, sql, 1, 1);
                conn.close();
            }
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
        es.awaitTermination(60, TimeUnit.SECONDS);
    }
}
