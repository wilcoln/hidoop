#!/bin/bash

# bobs : Bench On Bloc Size

app=$1

# Import utils
source $HIDOOP_HOME/bench/utils.sh

function bench_on_bloc_size {
    echo -ne "[0% completed]\r"
    for (( power=$start; power<=$end; power++ ))
    do
        factor=$(echo "2^"$power | bc)
        bloc_size=$(expr $factor \* 1000000)
        set_bloc_size $bloc_size
        echo "### WITH BLOC SIZE = "$bloc_size 1>> $results
        hidoop bench mb $input_size $app 1>> $results
        echo -ne "[$(expr $i \* 10)% completed]\r"
    done
    echo -ne '\n'
}

# On crée le dossier des résultats s'il n'existe pas
mkdir $HIDOOP_HOME/bench/results &> /dev/null

# On crée le fichier résultat
results=$HIDOOP_HOME/bench/results/bobs-$app
rm $results &> /dev/null
touch $results

# On fixe la taille du cluster
set_workers 5
echo "# FIXED ClUSTER SIZE : 5 workers" 1>> $results

# On fixe la taille du fichier d'entrée à 1GB
echo "# FIXED INPUT SIZE : 1GB" 1>> $results

# On fait varier la taille des blocs
start=1;end=10;input_size=1024
bench_on_bloc_size