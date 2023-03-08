package com.baidu.hive.conf;

import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.log.ErrorUtil;
import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ParseException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ConfDiff {

    public static void main(String [] args) throws HiveException, IOException,
            ParseException {
        HiveConf conf = new HiveConf();
        HiveTestUtils.addResource(conf, args);
        String[] defaultFiles = conf.get("conf.default-files", "").split(",");
        String[] confFiles =  conf.get("conf.files", "").split(",");
        if (defaultFiles.length == 0) {
            ErrorUtil.errorAndExit("Parameter conf.default-files must be configured.");
        }

        if (confFiles.length == 0) {
            ErrorUtil.errorAndExit("Parameter conf.files must be configured.");
        }

        LogUtil.log("");
        LogUtil.log("The differences between " + conf.get("conf.default-files", "") +
                " and " + conf.get("conf.files", ""));
        LogUtil.log("");

        Configuration defaultConf = new Configuration(false);
        for (String defaultFile : defaultFiles) {
            defaultConf.addResource(defaultFile.trim());
        }
        Configuration overrideConf = new Configuration(false);
        for (String confFile : confFiles) {
            overrideConf.addResource(confFile.trim());
        }
        Iterator<Map.Entry<String, String>> confIt = overrideConf.iterator();
        while (confIt.hasNext()) {
            Map.Entry<String, String> entry = confIt.next();
            String defaultValue = defaultConf.get(entry.getKey());
            String value = entry.getValue();

            if (defaultValue == null && value == null) {
                continue;
            }
            if (defaultValue != null && defaultValue.equals(value)) {
                continue;
            }
            LogUtil.log("Parameter key: " +  entry.getKey(),
                    "default value:" + defaultValue,
                    "current value:" + value);

            LogUtil.log("");
        }
    }
}
