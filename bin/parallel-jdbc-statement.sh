#!/usr/bin/env bash
# Parameters: "url" "execute times" "sql" "close connection"
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.jdbc.ParallelStatementTest \
 "jdbc:hive2://master-79c33a5-1:10000/default;principal=hive/_HOST@BAIDU.COM" \
 10 \
 "util.sm4encrypt('b001','123')" \
 true
