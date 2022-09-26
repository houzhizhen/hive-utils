#!/usr/bin/env bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.util.generator.PartitionGenerator \
     -hiveconf base-path=hdfs://localhost:9000/home/disk1/hive/hive-313/tp \
     -hiveconf part-names=dt,hour \
     -hiveconf sub-dirs=3,2 \
     -hiveconf files-in-partition=2 \
     -hiveconf file-size=1024

