#!/bin/bash

# monitor the live ojbect in hive server, and print TOTAL_MEMORY_BYTES
# The program runs in user hive.
cd $(dirname $0);

DELETE_LOGFILE=`date -d "-30 day" +"%Y%m%d"`
rm -rf ${DELETE_LOGFILE}*

HISTO_LOGFILE=`date +"%Y%m%d-%H%M%S"`-hive-server-histo-live.log
HIVESERVER_PID=`ps aux | grep HiveServer | grep -v grep | awk '{print $2}'` 

/opt/jdk1.8.0_211//bin/jmap -histo:live ${HIVESERVER_PID} > ${HISTO_LOGFILE}
export TOTAL_MEMORY_BYTES=`tail -n 1 ${HISTO_LOGFILE} | awk '{print $3}'`
echo ${TOTAL_MEMORY_BYTES}
