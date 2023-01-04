#!/bin/bash

# print the jstack of hiveserver to file.
# The program runs in user hive.

cd $(dirname $0);

DELETE_LOGFILE=`date -d "-30 day" +"%Y%m%d"`
rm -rf logs/${DELETE_LOGFILE}*

LOGFILE=logs/`date +"%Y%m%d-%H%M%S"`-hive-metastore-jstack.log
HIVE_METASTORE_PID=`ps aux | grep HiveMetaStore | grep -v grep | awk '{print $2}'` 

echo HIVE_METASTORE_PID=${HIVE_METASTORE_PID}  >> ${LOGFILE}
/opt/jdk1.8.0_211//bin/jstack ${HIVE_METASTORE_PID} >> ${LOGFILE}
