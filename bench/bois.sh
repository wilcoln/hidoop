#!/bin/bash

# bois: Bench On Input Size 

# Import utils
source $HIDOOP_HOME/bench/utils.sh

# Nettoyage et restart
hidoop clean
hidoop restart

# On crée le dossier des résultats s'il n'existe pas
mkdir $HIDOOP_HOME/bench/results &> /dev/null

results=$HIDOOP_HOME/bench/results/bois
rm $results &> /dev/null
touch $results

# On fixe la taille de blocs
set_bloc_size 128000000
echo "# FIXED BLOC SIZE : 128MB" &>> $results

# On fixe la taille du cluster
set_workers 5
echo "# FIXED ClUSTER SIZE : 5 workers" &>> $results

# On crée le fichier résultat


# On fait varier la taille des fichiers d'entrée
hidoop bench mb 64 &>> $results
hidoop bench mb 128 &>> $results
hidoop bench mb 256 &>> $results
hidoop bench mb 512 &>> $results
hidoop bench gb 1 &>> $results
hidoop bench gb 2 &>> $results
hidoop bench gb 5 &>> $results
hidoop bench gb 10 &>> $results