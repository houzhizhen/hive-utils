#!/bin/bash

# print the jstack of hiveserver to file.
# The program runs in user hive.

cd $(dirname $0);

DELETE_LOGFILE=`date -d "-30 day" +"%Y%m%d"`
rm -rf ${DELETE_LOGFILE}*

HISTO_LOGFILE=`date +"%Y%m%d-%H%M%S"`-hive-server-jstack.log
HIVESERVER_PID=`ps aux | grep HiveServer | grep -v grep | awk '{print $2}'` 

/opt/jdk1.8.0_211//bin/jstack ${HIVESERVER_PID} > ${HISTO_LOGFILE}
