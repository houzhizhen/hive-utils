package com.baidu.hive.driver.check;

import com.baidu.hive.driver.DriverBase;
import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.SQLUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.IDriver;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;
import org.apache.hadoop.hive.ql.processors.CommandProcessor;
import org.apache.hadoop.hive.ql.processors.CommandProcessorFactory;
import org.apache.hive.common.util.HiveStringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CheckSqlInPath extends DriverBase {

    private PrintWriter allSQLOut ;
    private PrintWriter partSQLOut;
    private PrintWriter pythonOut;
    private PrintWriter pySparkOut;
    private PrintWriter scalaOut;
    private PrintWriter nosqlOut;
    private final boolean sqlNeedParse;

    private int fileProcessed = 0;
    private int allSQLOutCount = 0;
    private int partSQLOutCount;
    private int pythonOutCount;
    private int pySparkOutCount;
    private int scalaOutCount;
    private int nosqlOutCount;
    private FileSystem fs;
    private Path root;

    public CheckSqlInPath(HiveConf conf) {
        super(conf);
        this.sqlNeedParse = conf.getBoolean("sql.need.parse", false);
        String rootDir = conf.get("root.dir");
        if (rootDir == null) {
            throw new RuntimeException("Must specify root.dir");
        }
        try {
            this.root = new Path(rootDir);
            this.fs = root.getFileSystem(conf);
            if (! this.fs.exists(this.root)) {
                throw new RuntimeException("root.dir " + this.root + " does not exist");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws HiveException, IOException, ParseException {
        HiveConf conf = new HiveConf();
        HiveTestUtils.addResource(conf, args);

        CheckSqlInPath generator = new CheckSqlInPath(conf);
        generator.createSession();
        try {
            generator.run();
        } finally {
            generator.closeSession();
        }
        System.exit(0);
    }

    public void run() throws IOException {
        generate();
        parseSQL(this.fs, "allSQL.out");
    }

    public void parseSQL(FileSystem fs, String localFile) throws IOException {
        List<Path> paths = SQLUtils.getPathsInFile(localFile);
        int totalSQLCount = 0;
        int sqlCanBeParsed = 0;
        int sqlCannotBeParsed = 0;
        PrintWriter errorLog = new PrintWriter(new FileWriter("parse-error.log"));
        Set<Path> errorPaths = new HashSet<>();
        for (Path path : paths) {
            String[] sqls = SQLUtils.getSQLsFroPath(fs, path);
            String sqlReplaced = "";
            for (String sql : sqls) {
                sql = sql.trim().toLowerCase();
                if (! sql.contains("select") || ! sql.contains("from")) {
                    continue;
                }
                sqlReplaced = replaceVariable(sql, "1");
                try {
                    totalSQLCount ++;
                    parseSql(sqlReplaced);
                    sqlCanBeParsed++;
                } catch (ParseException e) {
                    String message = getParseExceptionMessage(e);
                    errorPaths.add(path);
                    sqlCannotBeParsed++;

                    errorLog.println("origianl-sql:" + sql);
                    errorLog.println("sqlReplaced:" + sqlReplaced);
                    errorLog.println("path:" + path);
                    errorLog.println(message);
                    errorLog.println();
                }

            }
        }
        errorLog.close();
        System.out.println("Sql parse result, totalSQLCount:" + totalSQLCount + ", sqlCanBeParsed: " + sqlCanBeParsed
        + ", sqlCannotBeParsed:" + sqlCannotBeParsed);
        System.out.println("error path size :" + errorPaths.size());
        for (Path path: errorPaths) {
            System.out.println("error path:" + path.toString());
        }
    }

    public static String getParseExceptionMessage(ParseException e) {
        String message = e.getMessage();
        for (int count = 0; count < 2; count++) {
            int index = message.indexOf(' ');
            if (index < 0) {
                return message;
            } else {
                message = message.substring(index + 1);
            }
        }
        return message;
    }

    public void generate() throws IOException {
        this.allSQLOut = new PrintWriter(new FileWriter("allSQL.out"));
        this.partSQLOut = new PrintWriter(new FileWriter("partSQL.out"));
        this.nosqlOut = new PrintWriter(new FileWriter("nosql.out"));
        this.pythonOut = new PrintWriter(new FileWriter("python.out"));
        this.pySparkOut = new PrintWriter(new FileWriter("pySparkOut.out"));
        this.scalaOut = new PrintWriter(new FileWriter("scala.out"));

        try {
            List<Path> files = SQLUtils.getFiles(this.fs, this.root);
            for (Path path : files) {
                fileProcessed++;
                processFile(fs, path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            StringBuilder sb = new StringBuilder();
            sb.append("fileProcessed: ").append(this.fileProcessed)
                    .append(", allSQLOutCount: ").append(this.allSQLOutCount)
                    .append(", partSQLOutCount: ").append(this.partSQLOutCount)
                    .append(", nosqlOutCount: ").append(this.nosqlOutCount)
                    .append(", pythonOutCount: ").append(this.pythonOutCount)
                    .append(", pySparkOutCount: ").append(this.pySparkOutCount)
                    .append(", scalaOutCount: ").append(this.scalaOutCount)
                    .append(", sum:").append(allSQLOutCount + partSQLOutCount + nosqlOutCount
                            + pythonOutCount + pySparkOutCount + scalaOutCount );
            System.out.println(sb);
            allSQLOut.close();
            partSQLOut.close();
            nosqlOut.close();
            scalaOut.close();
            pythonOut.close();
            pySparkOut.close();
            scalaOut.close();
        }
    }

    public void processFile(FileSystem fs, Path path) {
        System.out.println("Processing " + path);
        String[] sqls = SQLUtils.getSQLsFroPath(fs, path);
        if (isPythonSpark(sqls)) {
            pySparkOut.println(path);
            pySparkOutCount++;
            return;
        }
        if (isPython(sqls)) {
            pythonOut.println(path);
            pythonOutCount++;
            return;
        }
        if (isScala(sqls)) {
            scalaOut.println(path);
            scalaOutCount++;
            return;
        }
        boolean hasSQL  = false;
        boolean hasNonSQL = false;
        for (String sql : sqls) {
            sql = sql.trim();
            if (sql.isEmpty()) {
                continue;
            } else if (sql.startsWith("--")) {
                continue;
            }
            if (isSql(sql)) {
                hasSQL = true;
            } else {
                hasNonSQL = true;
            }
        }
        if (hasSQL && !hasNonSQL) {
            allSQLOut.println(path.toString());
            allSQLOutCount++;
        } else if (hasSQL && hasNonSQL) {
            partSQLOut.println(path.toString());
            partSQLOutCount++;
        } else {
            nosqlOut.println(path.toString());
            nosqlOutCount++;
        }
    }

    public boolean isSql(String cmd) {
        cmd = replaceVariable(cmd, "1");
        String cmd_trimmed = HiveStringUtils.removeComments(cmd).trim();
        String[] tokens = tokenizeCmd(cmd_trimmed);
        if (cmd_trimmed.toLowerCase().equals("quit") || cmd_trimmed.toLowerCase().equals("exit")) {
            return true;
        } else if (tokens[0].equalsIgnoreCase("source")) {
            return true;
        } else if (cmd_trimmed.startsWith("!")) {
            return true;
        } else { // local mode
            try {
                CommandProcessor proc = CommandProcessorFactory.get(tokens, conf);
                if (proc == null) {
                    return false;
                }
                if (proc instanceof IDriver) {
                    // Let Driver strip comments using sql parser
                    // TODO: check can be parsed;
                    if (this.sqlNeedParse) {
                        try {
                            ParseUtils.parse(cmd, ctx);
                            return true;
                        } catch (ParseException e) {
                            System.err.println("parse parse sql:" + cmd);
                            return false;
                        }
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            } catch (SQLException e) {
                return false;
            }
        }
    }

    /**
     * throw ParseException if cannot parse
     * @param cmd
     * @return
     * @throws ParseException
     */
    public boolean parseSql(String cmd) throws ParseException {
        cmd = replaceVariable(cmd, "1");
        String cmd_trimmed = HiveStringUtils.removeComments(cmd).trim();
        String[] tokens = tokenizeCmd(cmd_trimmed);
        if (cmd_trimmed.toLowerCase().equals("quit") || cmd_trimmed.toLowerCase().equals("exit")) {
            return true;
        } else if (tokens[0].equalsIgnoreCase("source")) {
            return true;
        } else if (cmd_trimmed.startsWith("!")) {
            return true;
        } else { // local mode
            try {
                CommandProcessor proc = CommandProcessorFactory.get(tokens, conf);
                if (proc == null) {
                    return false;
                }
                if (proc instanceof IDriver) {
                    // Let Driver strip comments using sql parser
                    ParseUtils.parse(cmd, ctx);
                    return true;
                } else {
                    return true;
                }
            } catch (SQLException e) {
                return false;
            }
        }
    }

    public static String replaceVariable(String cmd, String value) {
        int from = 0;
        int beginIndex = cmd.indexOf("${", from);
        while (beginIndex >= 0) {
            int lastIndex = cmd .indexOf("}", beginIndex);
            if (lastIndex < 0) {
                throw new RuntimeException("can not replace variable for" + cmd);
            }
            StringBuilder sb = new StringBuilder();
            sb.append(cmd.substring(0, beginIndex));
            sb.append(value);
            sb.append(cmd.substring(lastIndex + 1));
            cmd = sb.toString();
            beginIndex = cmd.indexOf("${", from);
        }
        return cmd;
    }

    private static String[] tokenizeCmd(String cmd) {
        return cmd.split("\\s+");
    }

    private boolean isPythonSpark(String[] sqls) {
        for (String sql : sqls) {
            if (sql.contains("pyspark.sql")) {
                return true;
            }
        }
        return false;
    }

    private boolean isScala(String[] sqls) {
        for (String sql : sqls) {
            int index = sql.indexOf("def ");
            if (index == 0) {
                return true;
            }
            if (index > 0) {
                char preChar = sql.charAt(index - 1);
                if (preChar == '\n' || preChar == ' ') {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isPython(String[] lines) {
        for (String line : lines) {
            if (line.contains("/usr/bin/python")) {
                return true;
            }
            if (line.contains("__main__")) {
                return true;
            }
            if (line.contains("import os")) {
                return true;
            }
            if (line.contains("import math")) {
                return true;
            }
            if (line.contains("import time")) {
                return true;
            }
        }
        return false;
    }
}
