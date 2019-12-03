cd src
javac */*.java
java -Djava.rmi.server.hostname=`hostname` ordo.MapWorker