package com.baidu.hive.comiple;

import com.baidu.hive.util.log.LogUtil;
import org.apache.commons.cli.*;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CompilePerformanceTest implements Runnable {

    private String database;
    private String dir;
    private int times;
    Connection connection;
    Statement st;

    public CompilePerformanceTest(String database, String dir, int times) {
        this.database = database;
        this.dir = dir;
        this.times = times;
    }

    @Override
    public void run() {
        try {
            createConnection();
            long time1 = System.currentTimeMillis();
            runInteranl();
            long time2 = System.currentTimeMillis();
            LogUtil.log(String.format("Thread %s takes %s ms",
                                       Thread.currentThread().getId(),
                                       time2 - time1));
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() throws SQLException {
        connection.close();
    }

    private void createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        this.connection = DriverManager.getConnection("jdbc:hive2://localhost:10000/",
                "houzhizhen","houzhizhen");
    }

    private void runInteranl() throws ClassNotFoundException, SQLException {

        st = connection.createStatement();
        st.execute("use " + database);
        File parentFile = new File(dir);
        File[] files = parentFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".sql");
            }
        });
        for (int i = 0; i < times; i++) {
            LogUtil.log(String.format("Thread=%s, times=%s", Thread.currentThread().getName(), i));
            for (File file : files) {
                String str = getSQLFromFile(file);
                String[] sqls = str.split(";");
                for (String sql : sqls) {
                    if (sql.length() < 5) {
                        continue;
                    }
                    sql = "explain " + sql;
                    long time1 = System.currentTimeMillis();
                    st.execute(sql);
                    long time2 = System.currentTimeMillis();
                    LogUtil.log(String.format("Thread %s done file %s in %s ms",
                                              Thread.currentThread().getId(),
                                              file.getName(),
                                              time2 - time1));
                }
            }
        }
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
                qsb.append(line + "\n");
            }
        }
        return qsb.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        if (args == null || args.length == 0) {
            args = new String[]{
                    "--database", "tpcds_hdfs_orc_3",
                    "--directory", "/home/houzhizhen/git/hive-testbench/sample-queries-tpcds",
                    "--threadNum" ,"1",
                    "--iterators" ,"1"
            };
        }
        OptionsProcessor optionsProcessor = new OptionsProcessor();
        CommandLine cli = optionsProcessor.process(args);
        String database = cli.getOptionValue("database");
        String dir = cli.getOptionValue("directory");
        int threadNum = Integer.parseInt(cli.getOptionValue("threadNum"));
        int iterators = Integer.parseInt(cli.getOptionValue("iterators"));

        System.out.println(String.format("database=%s, dir=%s, thread=%s, iterators=%s",
                           database, dir, threadNum, iterators));

        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            executorService.execute(new CompilePerformanceTest(database, dir, iterators));
        }
        executorService.shutdown();
        executorService.awaitTermination(1L, TimeUnit.HOURS);
    }

    private static class OptionsProcessor {

        private final Options options = new Options();
        private org.apache.commons.cli.CommandLine commandLine;
        private Map<String, String> hiveVariables = new HashMap<String, String>();

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
                    .withArgName("threadNum")
                    .withLongOpt("threadNum")
                    .withDescription("Specify the thread number to compile parallel.")
                    .create("t"));
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
