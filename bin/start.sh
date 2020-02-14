#!/bin/bash

source ~/.bashrc

function start_master {
    java -classpath $HIDOOP_HOME/bin hdfs.NameNode &
}

function start_worker {
    java -classpath $HIDOOP_HOME/bin hdfs.DataNode &
    java -classpath $HIDOOP_HOME/bin ordo.MapWorker &
}

cnode=$1
case $cnode in
        'master')
          start_master
          ;;
        'worker')
          start_worker
          ;;
esac

