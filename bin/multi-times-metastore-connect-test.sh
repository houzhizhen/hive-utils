#!/usr/bin/env bash

hadoop jar ./hive-util-0.1.0.jar com.baidu.hive.metastore.MultiTimesMetaStoreConnectTest file:///usr/local/hive/conf/hive-site.xml ./test.xml
