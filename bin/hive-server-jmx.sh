#!/bin/bash

# monitor the jmx and live ojbect in hive server
cd $(dirname $0);pwd

DELETE_LOGFILE=`date -d "-1 day" +"%Y%m%d"`
rm -rf ${DELETE_LOGFILE}*
HIVESERVER_JMX="http://localhost:10002/jmx"
LOGFILE=`date +"%Y%m%d-%H%M%S"`-hive-server-jmx.log
curl ${HIVESERVER_JMX} > ${LOGFILE}
