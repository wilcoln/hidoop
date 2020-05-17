#!/bin/bash

# bois: Bench On Input Size 

app=$1

# Import utils
source $HIDOOP_HOME/bench/utils.sh

# On crée le dossier des résultats s'il n'existe pas
mkdir $HIDOOP_HOME/bench/results &> /dev/null

results=$HIDOOP_HOME/bench/results/bois-$app &> /dev/null
rm $results &> /dev/null
touch $results &> /dev/null

# On fixe la taille de blocs
set_bloc_size 128000000
echo "# FIXED BLOC SIZE : 128MB" 1>> $results

# On fixe la taille du cluster
set_workers 5
echo "# FIXED ClUSTER SIZE : 5 workers" 1>> $results

# On crée le fichier résultat


# On fait varier la taille des fichiers d'entrée
<<<<<<< HEAD
hidoop bench mb 64 $app &>> $results
 hidoop bench mb 128 $app &>> $results
 hidoop bench mb 256 $app &>> $results
 hidoop bench mb 512 $app &>> $results
 hidoop bench gb 1 $app &>> $results
 hidoop bench gb 2 $app &>> $results
 hidoop bench gb 5 $app &>> $results
 hidoop bench gb 10 $app &>> $results
=======
echo -ne '[0% completed]\r'
hidoop bench mb 64 $app 1>> $results
echo -ne '[1% completed]\r'
hidoop bench mb 128 $app 1>> $results
echo -ne '[2% completed]\r'
hidoop bench mb 256 $app 1>> $results
echo -ne '[4% completed]\r'
hidoop bench mb 512 $app 1>> $results
echo -ne '[8% completed]\r'
hidoop bench gb 1 $app 1>> $results
echo -ne '[16% completed]\r'
hidoop bench gb 2 $app 1>> $results
echo -ne '[32% completed]\r'
hidoop bench gb 5 $app 1>> $results
echo -ne '[60% completed]\r'
hidoop bench gb 10 $app 1>> $results
echo -ne '[100% completed]\r'
echo -ne '\n'
>>>>>>> fb539351a72168141583ae946d241ac08d178094
