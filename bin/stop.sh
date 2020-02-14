#!/bin/bash

source ~/.bashrc

function stop_master {
   pkill -f 'java.*NameNode*' &>/dev/null
}

function stop_worker {
  pkill -f 'java.*DataNode*' &>/dev/null
  pkill -f 'java.*MapWorker*' &>/dev/null
}
cnode=$1
case $cnode in
        'master')
          stop_master
          ;;
        'worker')
          stop_worker
          ;;
esac
