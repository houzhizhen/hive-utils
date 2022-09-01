package com.baidu.hive.util;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HiveTestUtils {

    /**
     * addResource in files specified by args to hiveConf
     * @param hiveConf
     * @param args
     */
    public static void addResource(HiveConf hiveConf, String[] args) {
        if (args == null) {
            return;
        }
        for (String arg : args) {
            File file = new File(arg);
            if (file.exists()) {
                System.out.println("Add resource " + arg);
                hiveConf.addResource(new Path(arg));
            }
        }
    }

    public static void printHiveConfByKeyOrder(HiveConf hiveConf) {
        List<String> keys = new ArrayList<>(hiveConf.size());
        for (Map.Entry<String, String> p : hiveConf) {
            keys.add(p.getKey());
        }
        Collections.sort(keys);
        System.out.println("printHiveConfByKeyOrder begin");
        for (String key : keys) {
            System.out.println(key + ":" + hiveConf.get(key));
        }
        System.out.println("printHiveConfByKeyOrder end");
        System.out.println("");
    }
}
