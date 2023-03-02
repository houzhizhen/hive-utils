package com.baidu.hive.util.generator;

import com.baidu.hive.driver.DriverBase;
import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.SQLUtils;
import com.baidu.hive.util.ast.ASTWalker;
import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GenerateSchemaFromSql extends DriverBase {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateSchemaFromSql.class);

    private final File inFile;
    private final File outFile;

    public GenerateSchemaFromSql(HiveConf conf) {
        super(conf);
        this.inFile = new File(conf.get("hive.generator.sql.input.file-name"));
        this.outFile = new File(conf.get("hive.generator.sql.output.file-name"));
    }

    public void generate() throws ParseException, IOException {
        if (!inFile.exists()) {
            LOG.error("Input file %s does not exist.", inFile.getName());
            System.exit(1);
        }
        if (outFile.exists()) {
            LOG.error("Output file %s already exist.", outFile.getName());
            System.exit(1);
        }
        String[] sqls = SQLUtils.getSQLsFromFile(inFile);

        for (String sql : sqls) {
            if ("".equals(sql.trim())) {
                continue;
            }
            ASTNode astNode = ParseUtils.parse(sql, ctx);
            System.out.println("astNode.getName()" + astNode.toString());
            if (!"TOK_QUERY".equals(astNode.toString())) {
                continue;
            }

            parseInputAndOutputTables(astNode);

            LogUtil.log(astNode.toStringTree());
        }

    }

    private void parseInputAndOutputTables(ASTNode astNode) throws IOException {
        ASTNode tokFrom = null;
        ASTNode tokInsert = null;
        List<Node> children = astNode.getChildren();
        for (int i = 0; i < children.size();i++) {
            ASTNode node = (ASTNode) children.get(i);
            if ("TOK_FROM".equals(node.toString())) {
                tokFrom = node;
            } else if ("TOK_INSERT".equals(node.toString())) {
                tokInsert = node;
            }
        }
        LogUtil.log("TOK_FROM:" + tokFrom == null ? "nil" : tokFrom.toStringTree());
        LogUtil.log("tokInsert:" + tokInsert == null ? "nil" : tokInsert.toStringTree());
        List<String> fromTables = getTabNames(tokFrom);
        List<String> insertTables = getTabNames(tokInsert);
        LogUtil.log("fromTables" + fromTables);
        LogUtil.log("insertTables" + insertTables);

        PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(this.outFile)));
        generateSchema(fromTables, printWriter);
        generateSchema(insertTables, printWriter);
    }

    private void generateSchema(List<String> fromTables, PrintWriter printWriter) {
    }

    private List<String> getTabNames(ASTNode astNode) {
        List<String> list = new ArrayList<>();
        ASTWalker.walk(astNode, (ASTNode node) -> {
            if (node.getType() == HiveParser.TOK_TABNAME) {
                List<? extends Node> subNodes = node.getChildren();
                if (subNodes.size() == 1) {
                    list.add(subNodes.get(0).toString());
                } else if (subNodes.size() == 2) {
                    list.add(subNodes.get(0).toString()+"." + subNodes.get(1).toString());
                }
            }
        });
        return list;
    }

    public static void main(String [] args) throws HiveException, IOException,
                                                   ParseException {
        HiveConf conf = new HiveConf();
        HiveTestUtils.addResource(conf, args);

        GenerateSchemaFromSql generator = new GenerateSchemaFromSql(conf);
        generator.createSession();
        try {
            generator.generate();
        } finally {
            generator.closeSession();
        }
        System.exit(0);
    }
}
