#!/usr/bin/env bash

hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.util.generator.TableDataGenerator \
 --hiveconf hive.generator.db-name=dxm \
 --hiveconf hive.generator.table-name=t1 \
 --hiveconf hive.generator.file-name=t1.txt \
 --hiveconf hive.generator.size=100
