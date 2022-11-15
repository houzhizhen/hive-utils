#!/bin/bash
export MYSQL_USERNAME=hive
export MYSQL_PASSWORD=xxxxxx
export MYSQL_HOST=localhost
export MYSQL_DB_NAME=hive
cd $(dirname $0);pwd

DELETE_LOGFILE=`date -d "-30 day" +"%Y%m%d"`
rm -rf ${DELETE_LOGFILE}*.log

LOGFILE=`date +"%Y%m%d-%H%M%S"`-delegation-monitor.log

{ time mysql -u${MYSQL_USERNAME} "-p${MYSQL_PASSWORD}" -h ${MYSQL_HOST} ${MYSQL_DB_NAME} -e "select count(1) from DELEGATION_TOKENS" >> ${LOGFILE} 2>&1; } >> ${LOGFILE} 2>&1


{ time mysql -u${MYSQL_USERNAME} "-p${MYSQL_PASSWORD}" -h ${MYSQL_HOST} ${MYSQL_DB_NAME} -e \
"select * from DELEGATION_TOKENS WHERE TOKEN = 'ABC'" \
>> $LOGFILE 2>&1; } >> $LOGFILE 2>&1
