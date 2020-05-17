#!/bin/bash

# bois: Bench On Input Size 

# Import utils
source $HIDOOP_HOME/bench/utils.sh

app=$1
# Nettoyage et restart
hidoop clean
#hidoop stop

# On crée le dossier des résultats s'il n'existe pas
mkdir $HIDOOP_HOME/bench/results &> /dev/null

results=$HIDOOP_HOME/bench/results/bois-$app
rm $results
touch $results

# On fixe la taille de blocs
set_bloc_size 128000000
echo "# FIXED BLOC SIZE : 128MB" &>> $results

# On fixe la taille du cluster
set_workers 5
echo "# FIXED ClUSTER SIZE : 5 workers" &>> $results

# On crée le fichier résultat


# On fait varier la taille des fichiers d'entrée
hidoop bench mb 64 $app &>> $results
 hidoop bench mb 128 $app &>> $results
 hidoop bench mb 256 $app &>> $results
 hidoop bench mb 512 $app &>> $results
 hidoop bench gb 1 $app &>> $results
 hidoop bench gb 2 $app &>> $results
 hidoop bench gb 5 $app &>> $results
 hidoop bench gb 10 $app &>> $results
