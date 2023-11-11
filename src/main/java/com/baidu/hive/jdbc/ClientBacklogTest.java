package com.baidu.hive.jdbc;

import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.hive.conf.HiveConf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ClientBacklogTest {
    private static String SERVER_IP = "whgg-inf-bce1134b39.whgg.baidu.com";
    private static int SERVER_PORT = 9092;
    private static final int DEFAULT_INTERVAL_SECONDS = 1;
    private static final int DEFAULT_PARALLELISM = 2;
    private static final AtomicLong totalTimeInMillions = new AtomicLong();
    private static final AtomicLong statementExecuted = new AtomicLong(0);

    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
        HiveConf conf = new HiveConf();
        HiveTestUtils.addResource(conf, args);

        int parallelism = conf.getInt("parallelism", DEFAULT_PARALLELISM);
        long interval = conf.getLong("intervalSeconds", DEFAULT_INTERVAL_SECONDS);
        SERVER_IP = conf.get("hive.server.ip", SERVER_IP);
        SERVER_PORT = conf.getInt("hive.server.port", SERVER_PORT);

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
            private int index = 0;
            @Override
            public void run() {
                long executed =  statementExecuted.get();
                if (executed == 0) {
                    LogUtil.log("avg time: executed = 0");
                } else {
                    LogUtil.log("index:" + index + ", avg time:" + (totalTimeInMillions.get() / executed) +
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
        Socket socket = null;
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            out.write(1);
            out.flush();

            int read = in.read();
            if (read != 2) {
                throw new RuntimeException("invalid length");
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            long time = System.currentTimeMillis() - beginTime;
            totalTimeInMillions.addAndGet(time);
            statementExecuted.incrementAndGet();
            if (socket != null) {
                try {
                    socket.close();
                }  catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
