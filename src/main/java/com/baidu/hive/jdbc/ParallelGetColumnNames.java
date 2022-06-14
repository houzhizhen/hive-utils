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

public class ParallelGetColumnNames extends GetColumnNames {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        String hiveUrl = "jdbc:hive2://localhost:10000/default";
        int times = 100;
        String tableName = "t1";
        String userName = "hive";
        boolean closeConnection = true;
        if (args.length >0) {
            hiveUrl = args[0];
        }
        if (args.length > 1) {
            times = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            tableName = args[2];
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
        LogUtil.log("tableName = " + tableName);
        LogUtil.log("closeConnection = " + closeConnection);
        LogUtil.log("userName = " + userName);
        Class.forName(HiveDriver.class.getName());
        AtomicBoolean error = new AtomicBoolean(false);
        try {
            for (int i = 0; i < times; i++) {
                LogUtil.log("i = " + i);
                ParallelGetColumnNames parallelGetColumnNames = new ParallelGetColumnNames();
                parallelGetColumnNames.createConnection();
                parallelGetColumnNames.parallelExecute(tableName, 2, 3);
                parallelGetColumnNames.closeConnection();
            }
        } catch(Throwable t) {
            error.set(true);
            t.printStackTrace();
        } finally {
            if (error.get()) {
                LogUtil.log("Error Finished");
            } else {
                LogUtil.log("Success Finished");
               TimeUnit.SECONDS.sleep(3600);
            }
        }
    }

    public void parallelExecute(String tableName, int parallelism, int times) throws InterruptedException {
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
                        for (int j = 0; j < times; j++) {
                            getColumnNames("hive", "default", tableName, "%");
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
