package com.baidu.hive.metastore;

import com.baidu.hive.metastore.api.DbMetastoreAPI;
import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.hive.conf.HiveConf;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.thrift.TException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MetaStoreCreateAndDropDbTest {

    private static final String THREAD_COUNT = "thread.count";
    private static final int THREAD_COUNT_DEFAULT = 2;
    private static final String METASTORE_CREATE_AND_DROP_DB_COUNT = "metastore.createAndDropDb.count";
    private static final int METASTORE_CONNECTION_COUNT_DEFAULT = 100;

    private static final String SLEEP_SECONDS_BETWEEN_OPERATION = "sleep.seconds.between.operation";
    private static final int SLEEP_SECONDS_BETWEEN_OPERATION_DEFAULT = 1;

    public static void main(String[] args) throws HiveException, TException, InterruptedException {

        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);

        int threadCount = hiveConf.getInt(THREAD_COUNT, THREAD_COUNT_DEFAULT);
        int opCount = hiveConf.getInt(METASTORE_CREATE_AND_DROP_DB_COUNT, METASTORE_CONNECTION_COUNT_DEFAULT);
        int sleepTime = hiveConf.getInt(SLEEP_SECONDS_BETWEEN_OPERATION, SLEEP_SECONDS_BETWEEN_OPERATION_DEFAULT);
        ExecutorService es = Executors.newFixedThreadPool(threadCount);
        AtomicInteger finishThreadCount = new AtomicInteger(0);
        AtomicInteger idGenerator = new AtomicInteger();
        AtomicInteger connectionCount = new AtomicInteger();
        for (int i = 0; i < threadCount; i++) {
            es.submit(() -> {
                int id = idGenerator.incrementAndGet();
                DbMetastoreAPI dbMetastoreAPI = new DbMetastoreAPI(hiveConf);
                dbMetastoreAPI.init();
                connectionCount.incrementAndGet();
                LogUtil.log("Metastore connections: " + connectionCount.get());
                int c = 0;
                String dbName = "db" + id;
                dbMetastoreAPI.dropDatabase(dbName, true);
                for (; c < opCount; c++) {
                    try {
                        dbMetastoreAPI.createDatabase(dbName);
                        dbMetastoreAPI.dropDatabase(dbName, false);
                    } catch (TException e) {
                        e.printStackTrace();
                        break;
                    }
                    try {
                        TimeUnit.SECONDS.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                connectionCount.decrementAndGet();
                System.out.println("Thread " + Thread.currentThread().getId() +
                        " finished, opcount = " + c + " finished thread count: " + finishThreadCount.incrementAndGet());
            });

        }
        long sleepSecond = 60L * 60L;
        es.shutdown();
        boolean success = es.awaitTermination(sleepSecond, TimeUnit.SECONDS);
        System.out.println("Finished, success = " + success);
    }
}
