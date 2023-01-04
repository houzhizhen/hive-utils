#!/bin/bash

# monitor the jmx and live ojbect in hive server
cd $(dirname $0);
DELETE_LOGFILE=`date -d "-30 day" +"%Y%m%d"`
rm -rf logs/${DELETE_LOGFILE}*
HIVESERVER_JMX="http://localhost:10002/jmx"
LOGFILE=logs/`date +"%Y%m%d-%H%M%S"`-hive-server-jmx.log

curl ${HIVESERVER_JMX} >> ${LOGFILE} 2>&1
