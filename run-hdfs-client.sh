cd src
javac */*.java
java -Djava.rmi.server.hostname=`hostname` hdfs.HdfsClient
rm -rf */*.class