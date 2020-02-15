#!/bin/bash

# bois: Bench On Input Size 

# Nettoyage et restart
hidoop clean
hidoop restart

results=$HIDOOP_HOME/bench/results/bois
rm $results
touch $results

# On fixe la taille de blocs
$HIDOOP_HOME/bench/utils/set-bs.sh 128000000
echo "# FIXED BLOC SIZE : 128MB" &>> $results

# On fixe la taille du cluster
cp $HIDOOP_HOME/bench/5w.xml $HIDOOP_HOME/config/core-site.xml
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