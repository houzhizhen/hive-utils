package com.baidu.hive.metastore.api;

import com.baidu.hive.metastore.MetaStoreUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.tez.common.Preconditions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MetastoreAPIBase {

    protected String catalogLocation;

    protected HiveConf conf;
    protected IMetaStoreClient client;

    public MetastoreAPIBase(HiveConf conf) {
        this.conf = conf;
    }

    public void init() {
        this.catalogLocation = conf.get("hive.metastore.warehouse.dir");
        if (catalogLocation.startsWith("/")) {
            String defaultFS = conf.get("fs.defaultFS");
            if (defaultFS.endsWith("/")) {
                defaultFS = defaultFS.substring(0, defaultFS.length() - 1);
            }
            this.catalogLocation = defaultFS  + catalogLocation;
        }
        try {
            this.client = MetaStoreUtil.createMetaStoreClient(this.conf);
        } catch (MetaException e) {
            log("Fatal: createMetaStoreClient failed");
            throw new RuntimeException(e);
        }
    }

    public void close() {
        this.client.close();
        try {
            CliSessionState.get().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log("MetaStoreApiTest closed");
    }

    private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void log(String... x) {
        for (String s : x) {
            System.out.println(format.format(new Date()) + " " + s);
        }
    }

    public static void logParameter(Configuration hiveConf, String parameterName) {
        log(parameterName + "=" + hiveConf.get(parameterName));
    }


    public static Map<String, String> generateParameters() {
        Map<String, String> parameters = new HashMap<>(10);
        for (int i = 1; i < 5; i++) {
            parameters.put("key" + i, String.valueOf(i));
        }
        return parameters;
    }

    public static String generateDescription() {
        return "fixed description";
    }


    public static Map<String, String> newMap(String... keyValuesPairs) {
        Preconditions.checkArgument(keyValuesPairs.length %2 == 0,
                "keyValuesPairs:" + Arrays.toString(keyValuesPairs) + ".length is not even");

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValuesPairs.length; i += 2) {
            map.put(keyValuesPairs[i], keyValuesPairs[i+1]);
        }
        return map;
    }
}
