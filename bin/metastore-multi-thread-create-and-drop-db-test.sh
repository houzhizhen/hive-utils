#!/usr/bin/env bash

hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.metastore.MetaStoreCreateAndDropDbTest \
     --hiveconf metastore.createAndDropDb.count=10 \
     --hiveconf sleep.seconds.between.operation=10 \
     --hiveconf thread.count=1
