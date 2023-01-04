#!/bin/bash

# The program runs in user hive.

cd $(dirname $0);
if [ ! -d logs ]; then
	mkdir logs
fi


# sh hive-metastore-jmx.sh
sh hive-metastore-jstack.sh
sh hive-metastore-histo.sh
sh hive-metastore-socket.sh
