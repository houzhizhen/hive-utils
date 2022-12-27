#!/usr/bin/env bash

hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.metastore.MultiThreadMetaStoreConnectTest \
     --hiveconf metastore.connection.count=7200 \
     --hiveconf thread.count=20
