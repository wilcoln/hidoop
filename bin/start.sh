#!/bin/bash

javac -d . ../src/*/*.java

function start_master {
  if jps | grep NameNode >/dev/null; then
    printf "A master is already running, Stop it first.\n"
  else
    echo "Starting NameNode..."
    exec -a hidoop-namenode-daemon java hdfs.NameNode &
  fi
}

function start_worker {
   if jps | grep DataNode >/dev/null; then
    printf "A worker is already running, Stop it first.\n"
    else
    echo "Starting DataNode..."
    exec -a hidoop-datanode-daemon java hdfs.DataNode &

    echo "Starting MapWorker..."
    exec -a hidoop-mapworker-daemon java ordo.MapWorker &
  fi
}
cnode=$1
  case $cnode in
  'master')
  start_master
    ;;
  'worker')
  start_worker
    ;;
  *)
    start_master
    start_worker
    ;;
  esac
