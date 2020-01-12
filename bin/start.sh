#!/bin/bash

javac -d . ../src/*/*.java

source ./common.sh
if  jps | grep NameNode >/dev/null
then
    printf "Hidoop is already running, Stop it first.\n"
else
    printf "Starting NameNode on master...\n"
    exec -a hidoop-namenode-daemon java hdfs.NameNode &
    printf "${green}NameNode Started${NC}\n"
    printf "Starting DataNodes...\n"
    exec -a hidoop-datanode-daemon java hdfs.DataNode &
    printf "${green}DataNode Started${NC}\n"
    printf "Starting MapWorkers..\n"
    exec -a hidoop-mapworker-daemon java ordo.MapWorker &
    printf "${green}MapWorker Started${NC}\n"
fi