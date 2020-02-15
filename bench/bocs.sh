#!/bin/bash

# bocs : Bench On Cluster Size

# On crée le fichier résultat
results=$HIDOOP_HOME/bench/results/bocs
rm $results
touch $results

# On fixe la taille de blocs
$HIDOOP_HOME/bench/utils/set-bs.sh 128000000
echo "# FIXED BLOC SIZE : 128MB" &>> $results

# On fixe la taille du fichier d'entrée
echo "# FIXED INPUT SIZE : 2GB" &>> $results

# On fait varier la taille du cluster
for i in {1..5}
do 
    hidoop clean
    hidoop stop
    cp $HIDOOP_HOME/bench/${i}w.xml $HIDOOP_HOME/config/core-site.xml
    echo "## WITH $i WORKER(S)" &>> $results
    hidoop start
    hidoop bench gb 2 &>> $results
done