#!/bin/bash

# monitor the jmx and live ojbect in hive server
cd $(dirname $0);pwd

DELETE_LOGFILE=`date -d "-1 day" +"%Y%m%d"`
rm -rf ${DELETE_LOGFILE}*
HIVESERVER_JMX="http://localhost:10002/jmx"
LOGFILE=`date +"%Y%m%d-%H%M%S"`-hive-server-jmx.log
curl ${HIVESERVER_JMX} > ${LOGFILE}

HISTO_LOGFILE=`date +"%Y%m%d-%H%M%S"`-hive-server-histo-live.log
HIVESERVER_PID=`ps aux | grep HiveServer | grep -v grep | awk '{print $2}'`

/opt/jdk1.8.0_211/bin/jmap -heap ${HIVESERVER_PID} > ${HISTO_LOGFILE}
