package com.baidu.hive.metastore;

import com.baidu.hive.util.HiveTestUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.thrift.TException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadMetaStoreConnectTest {

    private static final String THREAD_COUNT = "thread.count";
    private static final int THREAD_COUNT_DEFAULT = 20;
    private static final String METASTORE_CONNECTION_COUNT = "metastore.connection.count";
    private static final int METASTORE_CONNECTION_COUNT_DEFAULT = 100;

    public static void main(String[] args) throws HiveException, TException, InterruptedException {

        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);
        HiveTestUtils.printHiveConfByKeyOrder(hiveConf);

        int threadCount = hiveConf.getInt(THREAD_COUNT, THREAD_COUNT_DEFAULT);
        int connCount = hiveConf.getInt(METASTORE_CONNECTION_COUNT, METASTORE_CONNECTION_COUNT_DEFAULT);
        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        AtomicInteger finishThreadCount = new AtomicInteger(0);
        for (int i = 0; i < threadCount; i++) {
            es.submit(() -> {
                for (int c = 0; c < connCount; c++) {
                    long beginTime = System.currentTimeMillis();
                    try {
                        IMetaStoreClient metaStoreClient = MetaStoreUtil.createMetaStoreClient(hiveConf);
                        metaStoreClient.getAllDatabases();
                        metaStoreClient.close();
                    } catch (TException e) {
                        e.printStackTrace();
                    } finally {
                        long time = System.currentTimeMillis() - beginTime;
                        System.out.println("Thread " + Thread.currentThread().getId() + ", connect " + c + " takes " + time + " ms");
                    }
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Thread " + Thread.currentThread().getId() +
                                   " finished, finished thread count: " + finishThreadCount.incrementAndGet());
            });

        }
        long sleepSecond = 10 * 60L * 60L;
        es.shutdown();
        boolean success = es.awaitTermination(sleepSecond, TimeUnit.SECONDS);
        System.out.println("Finished, success = " + success);
    }
}
