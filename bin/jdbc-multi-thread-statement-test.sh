#!/usr/bin/env bash

# for kerberos: --hiveconf hiveUrl=jdbc:hive2://localhost:10000/default;principal=hive/_HOST@BAIDU.COM \
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.jdbc.MultiThreadStatementTest \
 --hiveconf hiveUrl=jdbc:hive2://localhost:10000/default \
 --hiveconf userName=hive \
 --hiveconf parallelism=2 \
 --hiveconf times=10 \
 --hiveconf 'sql=select 1'
