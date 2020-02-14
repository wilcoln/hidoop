order=$1
size=$2
generated=$size$order.txt


## Génération du fichier de test

data-gen $order $size &>/dev/null

echo "Taille fichier de test généré => "$(numfmt --to=iec-i --suffix=B --format="%.3f" $(stat --printf="%s" $generated))

## Write dans hdfs

start=`date +%s%3N`
hidoop write line $generated &>/dev/null
end=`date +%s%3N`

write_runtime=$((end-start))

echo "Temps d'exécution hdfs write :" $write_runtime"ms"


## Exécution Wordcount itératif

start=`date +%s%3N`
java -classpath $HIDOOP_HOME/bin application.Count $generated &>/dev/null
end=`date +%s%3N`

iter_runtime=$((end-start))

echo "Temps d'exécution wourdcount itératif :" $iter_runtime"ms"

## Exécution Wordcount MapRed

start=`date +%s%3N`
hidoop run application.MyMapReduce $generated &>/dev/null
end=`date +%s%3N`

job_runtime=$((end-start))

echo "Temps d'exécution wourdcount mapred :" $job_runtime"ms"


## Comparaison fichiers en sorti
sortie_iter="count.out."$generated 
sortie_mapred=$generated-reduce

echo  "Différences entre les sorties : " $(diff -y --suppress-common-lines $sortie_iter $sortie_mapred | grep '^' | wc -l)

# ## Read depuis hdfs

# start=`date +%s%3N`
# hidoop read $generated $generated.copy &>/dev/null 
# end=`date +%s%3N`

# read_runtime=$((end-start))

# echo "Temps d'exécution hdfs read :" $read_runtime"ms"

# ## Comparaison fichiers initial et fichier lu

# echo  "Différences entre le fichier original et le fichier lu :" $(diff -y --suppress-common-lines $generated $generated.copy | grep '^' | wc -l)

## Clean
rm -rf *$generated*
hidoop delete $generated
