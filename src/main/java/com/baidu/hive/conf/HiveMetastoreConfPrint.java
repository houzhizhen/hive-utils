package com.baidu.hive.conf;

import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.conf.MetastoreConf;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ParseException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class HiveMetastoreConfPrint {

    public static void main(String [] args)
            throws HiveException, IOException, ParseException {
        Configuration conf = MetastoreConf.newMetastoreConf();
        Iterator<Map.Entry<String, String>> it = conf.iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            LogUtil.log(entry.getKey() + ":" + entry.getValue());
        }
    }
}
