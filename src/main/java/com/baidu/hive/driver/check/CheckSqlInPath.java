package com.baidu.hive.driver.check;

import com.baidu.hive.driver.DriverBase;
import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.SQLUtils;
import com.baidu.hive.util.generator.GenerateSchemaFromSql;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;

import java.io.IOException;
import java.util.List;

import static com.baidu.hive.util.SQLUtils.filterOutSql;

public class CheckSqlInPath extends DriverBase {
    public CheckSqlInPath(HiveConf conf) {
        super(conf);
    }

    public static void main(String[] args) throws HiveException, IOException,
            ParseException {
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

    private void run() throws IOException {
        String rootDir = conf.get("root.dir");
        if (rootDir == null) {
            throw new RuntimeException("Must specify root.dir");
        }

        Path root = new Path(rootDir);
        String rootString = root.toString();
        FileSystem fs = root.getFileSystem(conf);
        if (! fs.exists(root)) {
            throw new RuntimeException("root.dir " + root + " does not exists");
        }
        List<Path> sqlFiles = SQLUtils.getFiles(fs, root);
        for (Path path : sqlFiles) {
            System.out.println("Processing " + path);
            String[] sqls = SQLUtils.getSQLsFroPath(fs, path);
            for (String sql : sqls) {
                if (filterOutSql(sql)) {
                    System.err.println("path has no sql:" + path.toString().substring(rootString.length() + 1));
                    continue;
                }
                try {
                    ParseUtils.parse(sql, ctx);
                    System.out.println("parse success path:" + path.toString().substring(rootString.length() + 1));
                } catch (ParseException e) {
                    System.err.println("parse error path:" + path.toString().substring(rootString.length() + 1));
                    // System.err.println("parse parse sql:" + sql);
                    break;
                }
            }
        }
    }




}
