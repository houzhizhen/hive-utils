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
    private static final boolean CREATE_CONNECTION_EACH_STATEMENT_DEFAULT = true;
    private static final boolean PRINT_LOG_EACH_STATEMENT_DEFAULT = true;

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
        ExecutorService es = Executors.newFixedThreadPool(parallelism);
        for (int i = 0; i < parallelism; i++) {
            es.submit(() -> {
                try {
                    execute(conf, error);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
        es.shutdownNow();
        boolean terminated = es.awaitTermination(1, TimeUnit.HOURS);
        LogUtil.log("terminated:" + terminated);
    }

    public static void execute(HiveConf conf, AtomicBoolean error) {

        boolean createConnectionEachStatement = conf.getBoolean("create-connection-each-statement",
                                                                CREATE_CONNECTION_EACH_STATEMENT_DEFAULT);
        if (createConnectionEachStatement) {
            createConnectionEachStatementExecute(conf, error);
        } else {
            singleConnectionForAllStatementsExecute(conf, error);
        }

    }
    private static void singleConnectionForAllStatementsExecute(HiveConf conf,
                                                                AtomicBoolean error) {
        String hiveUrl = conf.get("hiveUrl", HIVE_URL_DEFAULT);
        int times = conf.getInt("times", TIMES_DEFAULT);
        String sql = conf.get("sql", SQL_DEFAULT);
        String userName = conf.get("userName", USERNAME_DEFAULT);
        boolean isPrintLog = conf.getBoolean("print-log-each-statement",
                                             PRINT_LOG_EACH_STATEMENT_DEFAULT);
        Connection conn = null;
        try {

            conn = java.sql.DriverManager.getConnection(hiveUrl, userName, "hive");
            Statement st = conn.createStatement();
            for (int j = 0; j < times && !error.get(); j++) {
                st.executeQuery(sql);
                if (isPrintLog) {
                    LogUtil.log("Thread " + Thread.currentThread().getName() +
                                        " executed " + j + " times");
                }
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
    }
    private static void createConnectionEachStatementExecute(HiveConf conf, AtomicBoolean error) {
        String hiveUrl = conf.get("hiveUrl", HIVE_URL_DEFAULT);
        int times = conf.getInt("times", TIMES_DEFAULT);
        String sql = conf.get("sql", SQL_DEFAULT);
        String userName = conf.get("userName", USERNAME_DEFAULT);
        boolean isPrintLog = conf.getBoolean("print-log-each-statement",
                                             PRINT_LOG_EACH_STATEMENT_DEFAULT);
        Connection conn = null;
        try {
            for (int j = 0; j < times && !error.get(); j++) {
                conn = java.sql.DriverManager.getConnection(hiveUrl, userName, "hive");
                Statement st = conn.createStatement();
                st.executeQuery(sql);
                if (isPrintLog) {
                    LogUtil.log("Thread " + Thread.currentThread().getName() +
                                        " executed " + j + " times");
                }
                st.close();
                try {
                    conn.close();
                    conn = null;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

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
    }
}
