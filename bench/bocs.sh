#!/bin/bash

# bocs : Bench On Cluster Size

# On crée le fichier résultat
bocs_results=$HIDOOP_HOME/bench/bocs_results
rm $bocs_results
touch $bocs_results

# On fixe la taille de blocs
$HIDOOP_HOME/bench/change-bs.sh 128000000
echo "# FIXED BLOC SIZE : 128MB" &>> $bocs_results

# On fixe la taille du fichier d'entrée
echo "# FIXED INPUT SIZE : 2GB" &>> $bocs_results

# On fait varier la taille du cluster
for i in {1..5}
do 
    hidoop clean
    hidoop stop
    cp $HIDOOP_HOME/bench/${i}w.xml $HIDOOP_HOME/config/core-site.xml
    echo "## WITH $i WORKER(S)" &>> $bocs_results
    hidoop start
    hidoop bench gb 2 &>> $bocs_results
done