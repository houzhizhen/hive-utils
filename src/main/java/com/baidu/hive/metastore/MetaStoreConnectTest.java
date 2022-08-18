package com.baidu.hive.metastore;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.thrift.TException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MetaStoreConnectTest {

    public static void main(String[] args) throws HiveException, TException {
        HiveConf hiveConf = new HiveConf();
        for (String arg : args) {
            File file = new File(arg);
            if (file.exists()) {
                System.out.println("Add resource " + arg);
                hiveConf.addResource(new Path(arg));
            }
        }

        List<String> keys = new ArrayList<>(hiveConf.size());
        for (Map.Entry<String, String> p : hiveConf) {
            keys.add(p.getKey());
        }
        Collections.sort(keys);
        System.out.println("All conf begin");
        for (String key : keys) {
            System.out.println(key + ":" + hiveConf.get(key));
        }
        System.out.println("All conf end");
        System.out.println("");

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
