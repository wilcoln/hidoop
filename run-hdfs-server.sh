cd src
javac */*.java
java -Djava.rmi.server.hostname=`hostname` hdfs.HdfsServer
rm -rf */*.class