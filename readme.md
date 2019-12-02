# Settings
Editer le fichier config/Config.java, spécifier le master et les workers

**NB**: Pour le test, on peut spécifier un seul noeud, qui sera à la fois le master et l'unique worker.
# Lancement (Test)
Dans l'ordre:
1. Sur chaque worker faire ./run-hdfs-server.sh
2. Sur le master faire ./run-hdfs-client.sh
3. Sur chaque worker faire ./run-worker.sh
4. Sur le master faire ./run-mapred-app.sh