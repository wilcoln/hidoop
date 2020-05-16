#!/bin/bash

# bois: Bench On Input Size 
app=$1
appmapreduce=$2
# Nettoyage et restart
hidoop clean
#hidoop stop

results=$HIDOOP_HOME/bench/results/bois-$app
rm $results
touch $results

# On fixe la taille de blocs
$HIDOOP_HOME/bench/utils/set-bs.sh 128000000
echo "# FIXED BLOC SIZE : 128MB" &>> $results

# On fixe la taille du cluster
cp $HIDOOP_HOME/bench/utils/5w.xml $HIDOOP_HOME/config/core-site.xml
echo "# FIXED ClUSTER SIZE : 5 workers" &>> $results

# On crée le fichier résultat


# On fait varier la taille des fichiers d'entrée
hidoop bench mb 64 $app $appmapreduce &>> $results
hidoop bench mb 128 $app $appmapreduce &>> $results
hidoop bench mb 256 $app $appmapreduce &>> $results
hidoop bench mb 512 $app $appmapreduce &>> $results
hidoop bench gb 1 $app $appmapreduce &>> $results
hidoop bench gb 2 $app $appmapreduce &>> $results
hidoop bench gb 5 $app $appmapreduce &>> $results
hidoop bench gb 10 $app $appmapreduce &>> $results
