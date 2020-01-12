#!/bin/bash

source ./common.sh

if jps | grep NameNode >/dev/null; then
  printf "Stopping NameNode...\n"
  pkill -f hidoop-namenode-daemon
  printf "${orange}NameNode Stopped${NC}\n"
else
  printf "No Master running on host.\n"
fi

if jps | grep DataNode >/dev/null; then

  printf "Stopping DataNode...\n"
  pkill -f datanode-daemon
  printf "${orange}DataNode Stopped${NC}\n"

  printf "Stopping MapWorker...\n"
  pkill -f mapworker-daemon
  printf "${orange}MapWorker Stopped${NC}\n"
else
  printf "No Worker running on host.\n"
fi
