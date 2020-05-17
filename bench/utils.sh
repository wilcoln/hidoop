#!/bin/bash

function set_bloc_size {
    config="<?xml version=\"1.0\"?>
    <?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>

    <!-- Ici on précise les configurations liées à hdfs -->

    <configuration>
        <property>
            <name>rep-factor</name>
            <value>1</value>
        </property>
        <property>
            <name>bloc-size</name>
            <value>$1</value>
        </property>
        <property>
            <name>tmp-path</name>
            <value>/tmp/hidoop-tmp</value>
        </property>
        <property>
            <name>storage-path</name>
            <value>/tmp/hidoop-storage</value>
        </property>
        <property>
            <name>datanode-port</name>
            <value>3333</value>
        </property>
    </configuration>"

     echo -e "$config" > $HIDOOP_HOME/config/hdfs-site.xml
}

function set_workers {
    nb_workers="$1"
    workers_xml=""
    case $nb_workers in
        '1')
        workers_xml="<hostname>prevert.enseeiht.fr</hostname>"
        ;;
        '2')
        workers_xml="<hostname>prevert.enseeiht.fr</hostname>
                    <hostname>sand.enseeiht.fr</hostname>"
        ;;
        '3')
        workers_xml="<hostname>prevert.enseeiht.fr</hostname>
                    <hostname>sand.enseeiht.fr</hostname>
                    <hostname>verlaine.enseeiht.fr</hostname>"
        ;;
        '4')
        workers_xml="<hostname>prevert.enseeiht.fr</hostname>
                    <hostname>sand.enseeiht.fr</hostname>
                    <hostname>verlaine.enseeiht.fr</hostname>
                    <hostname>poe.enseeiht.fr</hostname>"
        ;;
        '5')
        workers_xml="<hostname>prevert.enseeiht.fr</hostname>
                    <hostname>sand.enseeiht.fr</hostname>
                    <hostname>verlaine.enseeiht.fr</hostname>
                    <hostname>poe.enseeiht.fr</hostname>
                    <hostname>mallarme.enseeiht.fr</hostname>"
        ;;
         *)
        exit
        ;;
    esac
    
    config="<?xml version=\"1.0\"?>
        <?xml-stylesheet type=\"text/xsl\" href=\"configuration.xsl\"?>

        <!-- Ici on précise les configurations du cluster en lui même -->

        <configuration>
            <property>
                <name>rmiregistry-port</name>
                <value>5021</value>
            </property>
            <property>
                <name>master</name>
                <value>
                    <hostname>kenobi.enseeiht.fr</hostname>
                </value>
            </property>
            <property>
                <name>workers</name>
                <value>
                    $workers_xml
                </value>
            </property>
        </configuration>"

    echo -e "$config" > $HIDOOP_HOME/config/core-site.xml
}