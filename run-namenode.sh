cd src
rm -rf */*.class
rm -rf file.line-*
rm -rf file.line.*
javac */*.java
java hdfs.NameNode
rm -rf */*.class