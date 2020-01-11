javac -d . ../src/*/*.java

gnome-terminal -x sh -c "java hdfs.NameNode"
sleep 0.25
gnome-terminal -x sh -c "java hdfs.DataNode"
sleep 0.25
gnome-terminal -x sh -c "java ordo.MapWorker"