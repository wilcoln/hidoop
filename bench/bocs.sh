#!/bin/bash

# bocs : Bench On Cluster Size

app=$1

# Import utils
source $HIDOOP_HOME/bench/utils.sh


# On crée le dossier des résultats s'il n'existe pas
mkdir $HIDOOP_HOME/bench/results &> /dev/null

# On crée le fichier résultat
results=$HIDOOP_HOME/bench/results/bocs-$app
rm $results &> /dev/null
touch $results

# On fixe la taille de blocs
set_bloc_size 128000000
echo "# FIXED BLOC SIZE : 128MB" 1>> $results

# On fixe la taille du fichier d'entrée
echo "# FIXED INPUT SIZE : 2GB" 1>> $results

# On fait varier la taille du cluster
echo -ne '[0% completed]\r'

for i in {5..5}
do 
    set_workers $i
    echo "## WITH $i WORKER(S)" 1>> $results
    hidoop bench gb 2 $app 1>> $results
    echo -ne "[$(expr $i \* 20)% completed]\r"
done

echo -ne '\n'