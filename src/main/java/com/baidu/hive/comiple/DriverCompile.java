package com.baidu.hive.comiple;

import com.baidu.hive.driver.DriverBase;
import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.SQLUtils;
import com.baidu.hive.util.log.LogUtil;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.DriverFactory;
import org.apache.hadoop.hive.ql.IDriver;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.util.Arrays;

public class DriverCompile extends DriverBase {

    private final String database;
    private final String dir;
    private final int times;
    private IDriver driver;

    public DriverCompile(HiveConf conf, String database,
                         String dir, int times) {
        super(conf);
        this.database = database;
        this.dir = dir;
        this.times = times;
    }

    public void execute() {
        this.createSession();
        this.driver = DriverFactory.newDriver(SessionState.get().getConf());
        driver.run("use  " + database);
        long time1 = System.currentTimeMillis();
        runInteranl();
        long time2 = System.currentTimeMillis();
        LogUtil.log(String.format("Thread %s takes %s ms",
                                  Thread.currentThread().getId(),
                                  time2 - time1));
        this.closeSession();
        System.exit(0);
    }

    private void runInteranl()  {
        File parentFile = new File(dir);
        File[] files = parentFile.listFiles((dir, name) -> name.endsWith(".sql"));

        if (files == null) {
            System.out.printf("The directory '%s' does not exist", dir);
            return;
        }
        Arrays.sort(files);
        for (int i = 0; i < times; i++) {
            LogUtil.log(String.format("Thread=%s, times=%s", Thread.currentThread().getName(), i));
            for (File file : files) {
                LogUtil.log("compile file:" + file.getName());
                String[] sqls = SQLUtils.getSQLsFromFile(file);
                for (String sql : sqls) {
                    if (sql.length() < 5) {
                        continue;
                    }
                    long time1 = System.currentTimeMillis();
                    driver.compile(sql);
                    long time2 = System.currentTimeMillis();
                    LogUtil.log(String.format("Thread %s done file %s in %s ms",
                                              Thread.currentThread().getId(),
                                              file.getName(),
                                              time2 - time1));
                }
            }
        }
    }

    public static void main(String[] args) {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);
        String database = hiveConf.get("database", "tpcds_hdfs_orc_3");
        String dir = hiveConf.get("path", ".");
        int iterators = Integer.parseInt(hiveConf.get("iterators", "1"));

        System.out.printf("database=%s, dir=%s, iterators=%s%n",
                          database, dir, iterators);

        new DriverCompile(hiveConf, database, dir, iterators).execute();
    }
}
