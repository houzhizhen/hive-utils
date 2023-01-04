#!/bin/bash

# monitor the live ojbect in hive metastore
# The program runs in user hive.
cd $(dirname $0);

DELETE_LOGFILE=`date -d "-30 day" +"%Y%m%d"`
rm -rf logs/${DELETE_LOGFILE}*

LOGFILE=logs/`date +"%Y%m%d-%H%M%S"`-hive-metastore-socket.log
HIVE_METASTORE_PID=`ps aux | grep HiveMetaStore | grep -v grep | awk '{print $2}'` 
echo HIVE_METASTORE_PID=${HIVE_METASTORE_PID}  >> ${LOGFILE}
netstat -tunp | grep ${HIVE_METASTORE_PID} >> ${LOGFILE} 2>&1
