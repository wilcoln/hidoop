#!/bin/bash

# bois: Bench On Input Size 

# Nettoyage et restart
hidoop clean
hidoop restart

bois_results=$HIDOOP_HOME/bench/bois_results
rm $bois_results
touch $bois_results

# On fixe la taille de blocs
$HIDOOP_HOME/bench/change-bs.sh 128000000
echo "# FIXED BLOC SIZE : 128MB" &>> $bois_results

# On fixe la taille du cluster
cp $HIDOOP_HOME/bench/5w.xml $HIDOOP_HOME/config/core-site.xml
echo "# FIXED ClUSTER SIZE : 5 workers" &>> $bois_results

# On crée le fichier résultat


# On fait varier la taille des fichiers d'entrée
hidoop bench mb 64 &>> $bois_results
hidoop bench mb 128 &>> $bois_results
hidoop bench mb 256 &>> $bois_results
hidoop bench mb 512 &>> $bois_results
hidoop bench gb 1 &>> $bois_results
hidoop bench gb 2 &>> $bois_results
hidoop bench gb 5 &>> $bois_results
hidoop bench gb 10 &>> $bois_results