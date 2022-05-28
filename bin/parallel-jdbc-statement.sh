#!/usr/bin/env bash
# Parameters: "url" "execute times" "sql" "close connection"
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.jdbc.ParallelStatementTest \
 "jdbc:hive2://localhost:10000/default" \
 1000 \
 "select * from t1 limit 1" \
 true
