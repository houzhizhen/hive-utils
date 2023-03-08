package com.baidu.hive.conf;

import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ParseException;

import java.io.IOException;

public class HiveConfDiff {

    public static void main(String [] args) throws HiveException, IOException,
            ParseException {

        HiveConf conf = new HiveConf();
        Configuration defaultConf = new Configuration(false);
        defaultConf.addResource("hive-default.xml");
        defaultConf.addResource("hivemetastore-default.xml");
        defaultConf.addResource("hiveserver2-default.xml");
        HiveConf.ConfVars[] vars = HiveConf.ConfVars.values();
        LogUtil.log("");
        LogUtil.log("");
        for (HiveConf.ConfVars confVar : vars) {
            String key = confVar.varname;
            String defaultValue = confVar.getDefaultValue();
            String value = HiveConf.getVar(conf, confVar);

            if (defaultValue == null && value == null) {
                continue;
            }
            if (value != null && value.equals(defaultValue)) {
                continue;
            }
            if (value.equals(defaultConf.get(key))) {
                continue;
            }
            LogUtil.log("Parameter key: " +  key,
                    "default value: " + defaultValue,
                    "current value: " + value,
                    "description: " + confVar.getDescription().replace("\n",""));

            LogUtil.log("");
        }
    }
}
