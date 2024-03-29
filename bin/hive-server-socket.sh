#!/bin/bash

# monitor the live ojbect in hive server
# The program runs in user hive.
cd $(dirname $0);

DELETE_LOGFILE=`date -d "-30 day" +"%Y%m%d"`
rm -rf logs/${DELETE_LOGFILE}*

LOGFILE=logs/`date +"%Y%m%d-%H%M%S"`-hive-server-socket.log
HIVESERVER_PID=`ps aux | grep HiveServer | grep -v grep | awk '{print $2}'` 

echo HIVESERVER_PID=${HIVESERVER_PID}  >> ${LOGFILE}
netstat -tunp | grep ${HIVESERVER_PID} >> ${LOGFILE} 2>&1
