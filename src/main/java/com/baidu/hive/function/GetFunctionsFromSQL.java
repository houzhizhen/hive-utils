package com.baidu.hive.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.SQLUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.Context;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.lockmgr.LockException;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;

import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.hadoop.hive.ql.wm.WmContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.hive.util.ast.ASTWalker;

public class GetFunctionsFromSQL {

    private static Logger LOG = LoggerFactory.getLogger("GetFunctionsFromSQL");
    private static int SQL_MIN_LENGTH = 5;

    public static void main(String[] args) throws IOException, LockException {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);

        String path = hiveConf.get("path");
        String suffix = hiveConf.get("suffix", "sql");
        LOG.info(String.format("path='%s', suffix='%s'", path, suffix));

        // Map of function to the files the function in.
        Map<String, List<String>> functionMap = new HashMap<>();
        File[] sqlFiles = getFileList(path, suffix);
        Context ctx = initContext();
        for (File sqlFile : sqlFiles) {
            String[] sqls = SQLUtils.getSQLsFromFile(sqlFile);
            for (String sql : sqls) {
                if (sql.length() < SQL_MIN_LENGTH) { // At least 5 characters.
                    continue;
                }
                Collection<String> functions = null;
                try {
                    functions = GetFunctionsFromSQL.functions(sql, ctx);
                } catch (Exception e) {
                    throw new RuntimeException("Can't get functions from " +
                                               sqlFile.getName(), e);
                }
                for (String function : functions) {
                    List<String> fileNames = functionMap.get(function);
                    if (fileNames == null) {
                        fileNames = new ArrayList<>();
                        functionMap.put(function, fileNames);
                    }
                    fileNames.add(sqlFile.getName());
                }
            }
        }
        printFunctionMap(functionMap);
    }

    public static Context initContext() throws IOException, LockException {
        HiveConf hiveConf = new HiveConf();
        SessionState ss = new SessionState(hiveConf, "mockUser");
        SessionState.start(ss);
        SessionState.setCurrentSessionState(ss);
        Context ctx = new Context(hiveConf);
        WmContext wmContext = new WmContext(System.currentTimeMillis(), "1");
        ctx.setWmContext(wmContext);
        ctx.setHiveTxnManager(SessionState.get().initTxnMgr(hiveConf));
        ctx.setStatsSource(null);

        ctx.setHDFSCleanup(true);
        return ctx;
    }

    public static Collection<String> functions(String sql, Context ctx)
            throws ParseException {

        Set<String> functions = new HashSet<>();
        ASTNode astNode = ParseUtils.parse(sql, ctx);
        ASTWalker.walk(astNode, (ASTNode node) -> {
            if (node.getType() == HiveParser.TOK_FUNCTION) {
                List<? extends Node> subNodes = node.getChildren();
                if (subNodes == null || subNodes.size() < 1) {
                    throw new RuntimeException("Can't get functions for sql: " +
                                               sql);
                }
                functions.add(subNodes.get(0).toString().toUpperCase());
            }
        });
        return functions;
    }

    private static void printFunctionMap(Map<String, List<String>> functionMap) {
        List<String> keys = new ArrayList<>();
        keys.addAll(functionMap.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            List<String> values = functionMap.get(key);
            Collections.sort(values, (o1, o2) -> {
                if (o1.length() < o2.length()) {
                    return -1;
                } else if (o1.length() > o2.length()) {
                    return 1;
                }
                return o1.compareTo(o2);
            });
            StringBuilder sb = new StringBuilder("| ")
                    .append(key)
                    .append(" | ")
                    .append(values.toString())
                    .append(" |");
            System.out.println(sb.toString());
        }
    }

    private static File[] getFileList(String path, String suffix) {
        File directory = new File(path);
        if (!directory.exists()) {
            printAndExit("Path " + path + " doesn't exist.");
        }
        if (directory.isFile()) {
            printAndExit("Path " + path + " is a file.");
        }
        return directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(suffix);
            }
        });
    }

    private static void printAndExit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
}
