#!/usr/bin/env bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.jdbc.ParallelStatementTest \
 --hiveconf hiveUrl=jdbc:hive2://localhost:10000/default \
 --hiveconf userName=hive \
 --hiveconf parallelism=2 \
 --hiveconf times=10 \
 --hiveconf 'sql=select 1'
