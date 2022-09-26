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
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-hiveconf".equals(arg) || "--hiveconf".equals(arg) && i < args.length - 1) {
                String keyValue = args[i + 1];
                int index = keyValue.indexOf("=");
                if (index != -1 && index != keyValue.length() - 1) {
                    hiveConf.set(keyValue.substring(0, index), keyValue.substring(index + 1));
                }
                i++;
            } else {
                File file = new File(arg);
                if (file.exists()) {
                    System.out.println("Add resource " + arg);
                    hiveConf.addResource(new Path(arg));
                }
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
