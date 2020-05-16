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
        workers_xml="<cluster-node>
                        <hostname>ewok.enseeiht.fr</hostname>
                        <ip-address>147.127.133.201</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>jabba.enseeiht.fr</hostname>
                        <ip-address>147.127.133.202</ip-address>
                    </cluster-node>"
        ;;
        '2')
        workers_xml="<cluster-node>
                        <hostname>ewok.enseeiht.fr</hostname>
                        <ip-address>147.127.133.201</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>jabba.enseeiht.fr</hostname>
                        <ip-address>147.127.133.202</ip-address>
                    </cluster-node>"
        ;;
        '3')
        workers_xml="<cluster-node>
                        <hostname>ewok.enseeiht.fr</hostname>
                        <ip-address>147.127.133.201</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>jabba.enseeiht.fr</hostname>
                        <ip-address>147.127.133.202</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>chewie.enseeiht.fr</hostname>
                        <ip-address>147.127.133.203</ip-address>
                    </cluster-node>"
        ;;
        '4')
        workers_xml="<cluster-node>
                        <hostname>ewok.enseeiht.fr</hostname>
                        <ip-address>147.127.133.201</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>jabba.enseeiht.fr</hostname>
                        <ip-address>147.127.133.202</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>chewie.enseeiht.fr</hostname>
                        <ip-address>147.127.133.203</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>lando.enseeiht.fr</hostname>
                        <ip-address>147.127.133.204</ip-address>
                    </cluster-node>"
        ;;
        '5')
        workers_xml="<cluster-node>
                        <hostname>ewok.enseeiht.fr</hostname>
                        <ip-address>147.127.133.201</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>jabba.enseeiht.fr</hostname>
                        <ip-address>147.127.133.202</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>chewie.enseeiht.fr</hostname>
                        <ip-address>147.127.133.203</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>lando.enseeiht.fr</hostname>
                        <ip-address>147.127.133.204</ip-address>
                    </cluster-node>
                    <cluster-node>
                        <hostname>dagobah.enseeiht.fr</hostname>
                        <ip-address>147.127.133.205</ip-address>
                    </cluster-node>"
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
                    <cluster-node>
                        <hostname>kenobi.enseeiht.fr</hostname>
                        <ip-address>147.127.133.200</ip-address>
                    </cluster-node>
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