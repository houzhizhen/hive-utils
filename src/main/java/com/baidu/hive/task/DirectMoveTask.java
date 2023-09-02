package com.baidu.hive.task;

import com.baidu.hive.util.HiveTestUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.thrift.TException;

import java.io.IOException;

/**
 * Move file from specified filesystem to local filesystem.
 * For example. hive --service jar hive-util-0.1.0.jar \
 * --hiveconf hive.move.task.source.path=bos:// \
 * --hiveconf hive.move.task.dest.path=/home/hive/out
 *
 */
public class DirectMoveTask {

    public static void main(String[] args) throws HiveException, TException, IOException {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);

        Path sourcePath = new Path(hiveConf.get("hive.move.task.source.path"));
        Path destPath = new Path(hiveConf.get("hive.move.task.dest.path"));
        FileSystem fs = sourcePath.getFileSystem(hiveConf);
        fs.copyToLocalFile(sourcePath, destPath);
    }
}
