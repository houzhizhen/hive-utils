package com.baidu.hive.util.generator;

import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.rand.RandomUtil;
import com.google.common.base.Preconditions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hive.conf.HiveConf;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generate partition directories and files in specified directories.
 * For example, basePath = "/user/hive/warehouse/tmp.db/t1"
 * partNames = ["dt", "hour"]
 * subDirOrFiles=[365, 24]
 * filesInPartition = 100.
 * It will generate the following directories, and each directory has 100 files.
 * ${basePath}/dt=0/hour=0/
 * ${basePath}/dt=0/hour=1/
 * ...
 * ${basePath}/dt=0/hour=23/
 * ${basePath}/dt=1/hour=0/
 * ${basePath}/dt=1/hour=1/
 * ...
 * ${basePath}/dt=1/hour=23/
 * ...
 */
public class PartitionGenerator {

    public static final Log LOG = LogFactory.getLog(PartitionGenerator.class);
    public static final String BASE_PATH = "base-path";
    public static final String PART_NAMES = "part-names";
    public static final String SUB_DIRS = "sub-dirs";
    public static final String FILES_IN_PARTITION = "files-in-partition";
    public static final int FILES_IN_PARTITION_DEFAULT = 10;
    public static final String FILE_SIZE = "file-size";
    public static final int FILE_SIZE_DEFAULT = 1024;

    private final String basePath;
    private final String[] partNames;
    private final int[] subDirs;
    private final int filesInPartition;
    private final List<String> partitionDirectoryList;
    private final int fileSize;

    public PartitionGenerator(Configuration conf) {
        String path = conf.get(BASE_PATH);
        Preconditions.checkNotNull(path, "The basePath can't be null");
        this.basePath = path.endsWith("/") ? path : path + "/";
        this.partNames = conf.getStrings(PART_NAMES);
        this.subDirs = conf.getInts(SUB_DIRS);
        this.filesInPartition = conf.getInt(FILES_IN_PARTITION, FILES_IN_PARTITION_DEFAULT);
        this.fileSize = conf.getInt(FILE_SIZE, FILE_SIZE_DEFAULT);

        Preconditions.checkArgument(partNames.length == subDirs.length,
                                    "partNames.length must equals subDirs.length");

        int total = subDirs[0];
        for (int i = 1; i < subDirs.length; i++) {
            total = total * subDirs[i];
        }
        this.partitionDirectoryList = new ArrayList<>(total);
    }

    public void generatePartNames(int level, String parentPaths) {
        String partName = partNames[level];
        for (int i = 0; i < subDirs[level]; i++) {
            String path = parentPaths + partName + "=" + i;
            if (level != subDirs.length - 1) {
                path += "/";
                generatePartNames(level + 1, path);
            } else {
                partitionDirectoryList.add(path);
            }
        }
    }

    public void makeSubDirsAndFiles() throws IOException {
        generatePartNames(0, "");
        Configuration conf = new HdfsConfiguration();
        FileSystem fs = FileSystem.get(URI.create(this.basePath), conf);
        fs.mkdirs(new Path(this.basePath));

        try {
            for (String partitionDirectory : partitionDirectoryList) {
                Path fullPath = new Path(this.basePath + partitionDirectory);
                fs.mkdirs(fullPath);
                createFiles(fs, fullPath);
            }
        } finally {
            fs.close();
        }
    }

    private void createFiles(FileSystem fs, Path path) throws IOException {
        for (int i = 0; i < filesInPartition; i++) {
            FSDataOutputStream out = fs.create(new Path(path, "file_" + i));
            out.write(RandomUtil.randomString(this.fileSize).getBytes(StandardCharsets.UTF_8));
            out.close();
        }
    }

    public static void main(String[] args) throws IOException {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);
        HiveTestUtils.printHiveConfByKeyOrder(hiveConf);
        PartitionGenerator generator = new PartitionGenerator(hiveConf);
        generator.makeSubDirsAndFiles();
    }
}
