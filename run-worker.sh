cd src
javac */*.java
rmiregistry 5021 &
java -Djava.rmi.server.hostname=192.168.122.1 ordo.HidoopWorkerImpl