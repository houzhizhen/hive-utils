package com.baidu.hive.metastore;

import com.baidu.hive.util.HiveTestUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.thrift.TException;

import java.util.List;

public class MetaStoreConnectTest {

    public static void main(String[] args) throws HiveException, TException {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);
        HiveTestUtils.printHiveConfByKeyOrder(hiveConf);

        Hive hive = Hive.get(hiveConf);
        IMetaStoreClient metaStoreClient = hive.getMSC();
        List<String> databases = metaStoreClient.getAllDatabases();
        System.out.println("All databases");
        for (String db : databases) {
            System.out.println(db);
        }
        metaStoreClient.close();
    }
}
