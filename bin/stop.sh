#!/bin/bash

source ./common.sh

if  jps | grep NameNode >/dev/null
then
    printf "Stopping NameNode on master...\n"
    bash -c "exec pkill -f hidoop-namenode-daemon"
    printf "${orange}NameNode Stopped${NC}\n"
    printf "Stopping DataNodes...\n"
    bash -c "exec pkill -f datanode-daemon"
    printf "${orange}DataNode Stopped${NC}\n"
    printf "Stopping MapWorkers...\n"
    bash -c "exec pkill -f mapworker-daemon"
    printf "${orange}MapWorker Stopped${NC}\n"
else
    printf "Hidoop is not running.\n"
fi