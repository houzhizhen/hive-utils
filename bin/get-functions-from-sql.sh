#!/usr/bin/env bash

hadoop jar ./hive-util-0.1.0.jar com.baidu.hive.function.GetFunctionsFromSQL \
  --path /Users/houzhizhen/git/hive-testbench/sample-queries-tpcds
