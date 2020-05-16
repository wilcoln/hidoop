#!/bin/bash

# bocs : Bench On Cluster Size

# Import utils
source $HIDOOP_HOME/bench/utils.sh

# On crée le dossier des résultats s'il n'existe pas
mkdir $HIDOOP_HOME/bench/results &> /dev/null

# On crée le fichier résultat
results=$HIDOOP_HOME/bench/results/bocs
rm $results &> /dev/null
touch $results

# On fixe la taille de blocs
set_bloc_size 128000000
echo "# FIXED BLOC SIZE : 128MB" &>> $results

# On fixe la taille du fichier d'entrée
echo "# FIXED INPUT SIZE : 2GB" &>> $results

# On fait varier la taille du cluster
for i in {1..5}
do 
    hidoop clean
    hidoop stop
    set_workers $i
    echo "## WITH $i WORKER(S)" &>> $results
    hidoop start
    hidoop bench gb 2 &>> $results
done