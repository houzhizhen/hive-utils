#!/bin/bash

# The program runs in user hive.

cd $(dirname $0);pwd

sh hive-server-jmx.sh
sh hive-server-jstack.sh
sh hive-server-histo.sh
