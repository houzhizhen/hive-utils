package com.baidu.hive.metastore;

import com.baidu.hive.util.HiveTestUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.RetryingMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MultiTimesMetaStoreConnectTest {

    private static final String METASTORE_CLIENT_CLASS = "hive.metastore.client.class";  //  The name of the class that implementing the IMetaStoreClient interface.
    private static final String METASTORE_CLIENT_CLASS_DEFAULT = "org.apache.hadoop.hive.ql.metadata.SessionHiveMetaStoreClient";

    private static final String METASTORE_CONNECTION_COUNT = "metastore.connection.count";
    private static final int METASTORE_CONNECTION_COUNT_DEFAULT = 10;
    public static void main(String[] args) throws HiveException, TException, InterruptedException {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);
        HiveTestUtils.printHiveConfByKeyOrder(hiveConf);

        int times = hiveConf.getInt(METASTORE_CONNECTION_COUNT, METASTORE_CONNECTION_COUNT_DEFAULT);
        List<IMetaStoreClient> metaStoreClientList = new ArrayList<>(times);
        for (int i = 0; i < times; i++) {
            IMetaStoreClient metaStoreClient = MetaStoreUtil.createMetaStoreClient(hiveConf);
            metaStoreClientList.add(metaStoreClient);
        }
        System.out.println("Loop");
        for (int i = 0; i < times; i++) {
            IMetaStoreClient metaStoreClient = metaStoreClientList.get(i);
            System.out.println("metaStoreClient" + i + ", hashCode = " +  System.identityHashCode(metaStoreClient));
            List<String> databases = metaStoreClient.getAllDatabases();
            System.out.println("All databases");
            for (String db : databases) {
                System.out.println(db);
            }
            metaStoreClient.close();
        }
        System.out.println("Finished");

        long sleepSecond = 60L * 60L;
        System.out.println("Enter sleep, sleepSecond=" + sleepSecond);
        TimeUnit.SECONDS.sleep(sleepSecond);
    }
}
