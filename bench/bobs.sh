#!/bin/bash

# bobs : Bench On Bloc Size

# Import utils
source $HIDOOP_HOME/bench/utils.sh

# Nettoyage et restart
hidoop clean
hidoop restart

function bench_on_bs {
    for (( power=$start; power<=$end; power++ ))
    do
        factor=$(echo "2^"$power | bc)
        bs=$(expr $factor \* 1000000)
        set_bloc_size $bs
        echo "### WITH BLOC SIZE = "$bs &>> $results
        hidoop bench mb $input_size &>> $results
    done
}

# On crée le dossier des résultats s'il n'existe pas
mkdir $HIDOOP_HOME/bench/results &> /dev/null

# On crée le fichier résultat
results=$HIDOOP_HOME/bench/results/bobs
rm $results &> /dev/null
touch $results

# On fixe la taille du cluster
set_workers 5
echo "# FIXED ClUSTER SIZE : 5 workers" &>> $results

# On fixe la taille du fichier d'entrée
echo "# FIXED INPUT SIZE : 512MB" &>> $results

# On fait varier la taille des blocs
start=0;end=9;input_size=512
bench_on_bs


# On fixe la taille du fichier d'entrée à 1GB
echo "# FIXED INPUT SIZE : 1GB" &>> $results

# On fait varier la taille des blocs
start=1;end=10;input_size=1024
bench_on_bs