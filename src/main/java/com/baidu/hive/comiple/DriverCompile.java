package com.baidu.hive.comiple;

import com.baidu.hive.util.log.LogUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.common.io.CachingPrintStream;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.DriverFactory;
import org.apache.hadoop.hive.ql.IDriver;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

public class DriverCompile {

    private final String database;
    private final String dir;
    private final int times;
    private IDriver driver;

    public DriverCompile(String database, String dir, int times) {
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

    private void closeSession() {
        try {
            SessionState.get().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runInteranl()  {
        File parentFile = new File(dir);
        File[] files = parentFile.listFiles((dir, name) -> name.endsWith(".sql"));
        if (files == null) {
            System.out.printf("The directory '%s' does not exist", dir);
            return;
        }
        for (int i = 0; i < times; i++) {
            LogUtil.log(String.format("Thread=%s, times=%s", Thread.currentThread().getName(), i));
            for (File file : files) {
                String str = getSQLFromFile(file);
                String[] sqls = str.split(";");
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
        OptionsProcessor optionsProcessor = new OptionsProcessor();
        CommandLine cli = optionsProcessor.process(args);
        String database = cli.getOptionValue("database", "tpcds_hdfs_orc_3");
        String dir = cli.getOptionValue("directory", ".");
        int iterators = Integer.parseInt(cli.getOptionValue("iterators", "1"));

        System.out.printf("database=%s, dir=%s, iterators=%s%n",
                          database, dir, iterators);

        new DriverCompile(database, dir, iterators).execute();
    }

    private void createSession() {
        HiveConf conf = new HiveConf(SessionState.class);

        for (Map.Entry<String, String> entry : conf) {
            LogUtil.log("key:" + entry.getKey() + ", value:" + entry.getValue());
        }
        CliSessionState ss = new CliSessionState(conf);
        ss.in = System.in;
        try {
            ss.out = new PrintStream(System.out, true, "UTF-8");
            ss.info = new PrintStream(System.err, true, "UTF-8");
            ss.err = new CachingPrintStream(System.err, true, "UTF-8");
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        CliSessionState.start(ss);
    }

    public static String getSQLFromFile(File file) {
        BufferedReader bufferReader = null;
        try {
            bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            return getSQLFromReader(bufferReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeStream(bufferReader);
        }
    }

    public static String getSQLFromReader(BufferedReader r) throws IOException {
        String line;
        StringBuilder qsb = new StringBuilder();

        while ((line = r.readLine()) != null) {
            // Skipping through comments
            if (! line.startsWith("--")) {
                qsb.append(line).append("\n");
            }
        }
        return qsb.toString();
    }


    private static class OptionsProcessor {

        private final Options options = new Options();

        @SuppressWarnings("static-access")
        private OptionsProcessor() {
            // -database database
            options.addOption(OptionBuilder
                                      .hasArg()
                                      .withArgName("database")
                                      .withLongOpt("database")
                                      .withDescription("Database name")
                                      .create('d'));
            options.addOption(OptionBuilder
                                      .hasArg()
                                      .withArgName("dir")
                                      .withLongOpt("directory")
                                      .withDescription("Specify the directory in which the sql file located")
                                      .create());
            options.addOption(OptionBuilder
                                      .hasArg()
                                      .withArgName("iterators")
                                      .withLongOpt("iterators")
                                      .withDescription("Specify the iterator times to loop compile the files in specified directory.")
                                      .create("i"));
        }

        public CommandLine process(String[] args) {
            try {
                return new GnuParser().parse(options, args);
            } catch (ParseException e) {
                throw new RuntimeException("Can't parse args:" + Arrays.toString(args));
            }
        }
    }
}
