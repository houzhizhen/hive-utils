package com.baidu.hive.comiple;

import com.baidu.hive.driver.DriverBase;
import com.baidu.hive.util.HiveTestUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.Context;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;

import java.io.IOException;

public class PrintAstOfSql extends DriverBase {

    private static final String HIVE_SQL = "hive.sql";

    public PrintAstOfSql(HiveConf conf) {
        super(conf);
    }
    public void execute() {
        String command = conf.get(HIVE_SQL);
        ASTNode tree = null;
        try {
            tree = ParseUtils.parse(command, ctx);
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {

        }
        if (tree != null) {
            System.out.println(tree.toStringTree());
        } else {
            System.out.println("tree is null");
        }
    }

    public static void main(String[] args) throws IOException {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);
        PrintAstOfSql printAstOfSql = new PrintAstOfSql(hiveConf);
        printAstOfSql.createSession();
        printAstOfSql.execute();
        printAstOfSql.closeSession();
    }
}
