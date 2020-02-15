#!/bin/bash

rm -rf /tmp/hidoop-storage/* /tmp/hidoop-tmp/*

while read hostname; do
    ssh-copy-id $hostname &> /dev/null
    ssh -f $hostname "rm -rf /tmp/hidoop-storage/*"
done <$HIDOOP_HOME/config/.workers