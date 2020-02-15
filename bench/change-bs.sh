#!/bin/bash

cat  > $HIDOOP_HOME/config/hdfs-site.xml << EOF
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<!-- Ici on précise les configurations liées à hdfs -->

<configuration>
    <property>
        <name>rep-factor</name>
        <value>1</value>
        <description></description>
    </property>
    <property>
        <name>bloc-size</name>
        <value>$1</value>
        <description></description>
    </property>
    <property>
        <name>tmp-path</name>
        <value>/tmp/hidoop-tmp</value>
        <description></description>
    </property>
    <property>
        <name>storage-path</name>
        <value>/tmp/hidoop-storage</value>
        <description></description>
    </property>
    <property>
        <name>datanode-port</name>
        <value>3333</value>
        <description></description>
    </property>
</configuration>
EOF