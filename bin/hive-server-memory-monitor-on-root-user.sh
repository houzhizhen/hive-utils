#!/bin/bash
# Monitor the TOTAL_MEMORY_BYTES, and compare with the specified limit, if more than limit, restart hiveserver.
# The program runs on user root.
PWD=`cd $(dirname $0);pwd`
cd $PWD
#HIVE_SERVER_MEMORY_LIMIT=30G
export HIVE_SERVER_MEMORY_LIMIT=30000000000

DELETE_LOGFILE=`date -d "-30 day" +"%Y%m%d"`
rm -rf ${DELETE_LOGFILE}*

MONITOR_LOGFILE=${PWD}/`date +"%Y%m%d-%H%M%S"`-hive-server-monitor_on_user_root.log
export TOTAL_MEMORY_BYTES=`su - hive -c "sh ${PWD}/hive-server-memory-bytes.sh"`
echo TOTAL_MEMORY_BYTES=${TOTAL_MEMORY_BYTES} >> ${MONITOR_LOGFILE}
echo TOTAL_MEMORY_BYTES=${TOTAL_MEMORY_BYTES}

if [ $TOTAL_MEMORY_BYTES -gt ${HIVE_SERVER_MEMORY_LIMIT} ]; then
  echo TOTAL_MEMORY_BYTES:${TOTAL_MEMORY_BYTES} more than HIVE_SERVER_MEMORY_LIMIT:${HIVE_SERVER_MEMORY_LIMIT} >> ${MONITOR_LOGFILE}
  echo STARTING HIVESERVER >> ${MONITOR_LOGFILE}
  systemctl restart hive-server2
else
  echo ${TOTAL_MEMORY_BYTES}  no more than LIMIT:${HIVE_SERVER_MEMORY_LIMIT} >> ${MONITOR_LOGFILE}
fi