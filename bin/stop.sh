#!/bin/bash

source ~./zshrc

function stop_master {
   pkill -f 'java.*NameNode*'
}

function stop_worker {
  pkill -f 'java.*DataNode*'
  pkill -f 'java.*MapWorker*'
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
