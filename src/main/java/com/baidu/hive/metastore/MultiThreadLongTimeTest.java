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

public class MultiThreadLongTimeTest {

    private static final String THREAD_COUNT = "thread.count";
    private static final int THREAD_COUNT_DEFAULT = 20;
    private static final String METASTORE_API_EXEC_COUNT_PER_INDEX = "metastore.api.exec.count.per-thread";
    private static final int METASTORE_API_EXEC_COUNT_PER_INDEX_DEFAULT = 1000;

    private static final String METASTORE_API_LOG_EVERY_N_CALLS = "metastore.api.log.every.n-calls";
    private static final int METASTORE_API_LOG_EVERY_N_CALLS_DEFAULT = 1000;

    public static void main(String[] args) throws HiveException, TException, InterruptedException {

        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);

        int threadCount = hiveConf.getInt(THREAD_COUNT, THREAD_COUNT_DEFAULT);
        int execCount = hiveConf.getInt(METASTORE_API_EXEC_COUNT_PER_INDEX, METASTORE_API_EXEC_COUNT_PER_INDEX_DEFAULT);
        int logEevryNCalls = hiveConf.getInt(METASTORE_API_LOG_EVERY_N_CALLS, METASTORE_API_LOG_EVERY_N_CALLS_DEFAULT);
        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        AtomicInteger finishThreadCount = new AtomicInteger(0);
        for (int i = 0; i < threadCount; i++) {
            es.submit(() -> {
                long beginTime = System.currentTimeMillis();
                try {
                    IMetaStoreClient metaStoreClient = MetaStoreUtil.createMetaStoreClient(hiveConf);
                    for (int execIndex = 0; execIndex < execCount; execIndex++) {
                        metaStoreClient.getAllDatabases();
                        if (execIndex % logEevryNCalls == 0) {
                            System.out.println("Thread " + Thread.currentThread().getId() +
                                                       ", called " + execIndex + " times, takes " +
                                                       (System.currentTimeMillis() - beginTime) + " ms");
                        }
                    }
                    metaStoreClient.close();
                } catch (TException e) {
                    e.printStackTrace();
                } finally {

                }
                long time = System.currentTimeMillis() - beginTime;
                System.out.println("Thread " + Thread.currentThread().getId() + ", connect  takes " + time + " ms");
                System.out.println("Thread " + Thread.currentThread().getId() +
                        " finished, finished thread count: " + finishThreadCount.incrementAndGet());
            });

        }
        long sleepSecond = 60L * 60L;
        es.shutdown();
        boolean success = es.awaitTermination(sleepSecond, TimeUnit.SECONDS);
        System.out.println("Finished, success = " + success);
    }
}
