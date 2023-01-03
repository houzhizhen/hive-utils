#!/bin/bash

# monitor the live ojbect in hive server
# The program runs in user hive.
cd $(dirname $0);

DELETE_LOGFILE=`date -d "-30 day" +"%Y%m%d"`
rm -rf logs/${DELETE_LOGFILE}*

HISTO_LOGFILE=logs/`date +"%Y%m%d-%H%M%S"`-hive-server-socket.log
HIVESERVER_PID=`ps aux | grep HiveServer | grep -v grep | awk '{print $2}'` 

netstat -tunp | grep ${HIVESERVER_PID} > ${HISTO_LOGFILE}
