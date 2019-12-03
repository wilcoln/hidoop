cd src
javac */*.java
java -Djava.rmi.server.hostname=$1 hdfs.HdfsClient
rm -rf */*.class