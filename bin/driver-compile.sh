#!/usr/bin/env bash

hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.comiple.DriverCompile \
    --hiveconf database=tpcds_hdfs_orc_3 \
    --hiveconf directory=/home/hive/hive-testbench/sample-queries-tpcds/ \
    --hiveconf iterators=1
