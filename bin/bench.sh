#!/bin/bash

order=$1
size=$2
app=$3
appmapreduce=$4
generated=$size$order.txt

# Nettoyage & restart
hidoop clean &> /dev/null
hidoop restart &> /dev/null

## Génération du fichier de test

data-gen $order $size $app &> /dev/null

echo "Taille fichier de test généré => "$(numfmt --to=iec-i --suffix=B --format="%.3f" $(stat --printf="%s" $generated))

## Write dans hdfs

start=`date +%s%3N`
hidoop write line $generated &> /dev/null
end=`date +%s%3N`

write_runtime=$((end-start))

echo "Temps d'exécution hdfs write :" $write_runtime"ms"


## Exécution application itératif

start=`date +%s%3N`
java -classpath $HIDOOP_HOME/bin application.$app $generated &> /dev/null
end=`date +%s%3N`

iter_runtime=$((end-start))

echo "Temps d'exécution "$app" séquentiel:" $iter_runtime"ms"

## Exécution application MapRed

start=`date +%s%3N`
hidoop run application.$app"MR" $generated &> /dev/null
end=`date +%s%3N`

job_runtime=$((end-start))

echo "Temps d'exécution "$app" mapred:" $job_runtime"ms"


## Comparaison fichiers en sorti
sortie_iter=$app".out."$generated 
sortie_mapred=$generated-reduce

echo  "Différences entre les sorties : " $(diff -y --suppress-common-lines $sortie_iter $sortie_mapred | grep '^' | wc -l)

## Nettoyage
rm -rf *$generated*
hidoop delete $generated
hidoop clean
