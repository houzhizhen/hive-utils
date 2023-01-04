#!/bin/bash

# The program runs in user hive.

cd $(dirname $0);

if [ ! -d logs ]; then
	mkdir logs
fi

sh hive-server-jmx.sh
sh hive-server-jstack.sh
sh hive-server-histo.sh
sh hive-server-socket.sh
