order=$1
size=$2
generated=$size$order.txt

## Création du fichier de test
data-gen $order $size &>/dev/null

## Traitement 

## Write dans hdfs
start=`date +%s%3N`
hidoop write $generated &>/dev/null
end=`date +%s%3N`

write_runtime=$((end-start))

echo "Temps exécution write : " $write_runtime"ms"

## Version MapRed
start=`date +%s%3N`
hidoop run application.MyMapReduce $generated &>/dev/null
end=`date +%s%3N`

job_runtime=$((end-start))

# Version itérative 
start=`date +%s%3N`
java -classpath $HIDOOP_HOME/bin application.Count $generated &>/dev/null
end=`date +%s%3N`

iter_runtime=$((end-start))

# Comparaison des temps d'exécution
echo "Temps exécution job : " $job_runtime"ms"

echo "Temps exécution itératif : " $iter_runtime"ms"

# Comparaison fichiers en sorti
diff "count.out."$generated $generated-reduce > diff.out

echo  "Différences fichiers sorties" $(wc -l diff.out | awk '{ print $1 }') 

# Clean
rm -rf /tmp/hidoop-*
rm -rf *$generated*
hidoop delete $generated
rm diff.out
