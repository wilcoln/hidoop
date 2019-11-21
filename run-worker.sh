cd src
javac */*.java
java -Djava.rmi.server.hostname=$0 ordo.HidoopWorkerImpl