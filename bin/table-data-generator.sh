#!/usr/bin/env bash

hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.util.generator.TableDataGenerator test t1 t1.txt 100
