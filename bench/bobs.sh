#!/bin/bash

source ~/.bashrc

# On fixe la taille du fichier d'entrée
input_size=2

# On fixe la taille du cluster
cp $HIDOOP_HOME/bench/5w.xml $HIDOOP_HOME/config/core-site.xml

# On fait varier la taille des blocs
bobs_results=$HIDOOP_HOME/bench/bobs_results
touch $bobs_results

# $HIDOOP_HOME/bench/change-bs.sh 1000000
# hidoop restart
# hidoop bench mb 512 &>> $bobs_results

# $HIDOOP_HOME/bench/change-bs.sh 2000000
# hidoop restart
# hidoop bench mb 512 &>> $bobs_results

# $HIDOOP_HOME/bench/change-bs.sh 4000000
# hidoop restart
# hidoop bench mb 512 &>> $bobs_results

# $HIDOOP_HOME/bench/change-bs.sh 8000000
# hidoop restart
# hidoop bench mb 512 &>> $bobs_results

$HIDOOP_HOME/bench/change-bs.sh 16000000
hidoop restart
hidoop bench mb 512 &>> $bobs_results

$HIDOOP_HOME/bench/change-bs.sh 32000000
hidoop restart
hidoop bench mb 512 &>> $bobs_results

$HIDOOP_HOME/bench/change-bs.sh 64000000
hidoop restart
hidoop bench mb 512 &>> $bobs_results

$HIDOOP_HOME/bench/change-bs.sh 128000000
hidoop restart
hidoop bench mb 512 &>> $bobs_results

$HIDOOP_HOME/bench/change-bs.sh 512000000
hidoop restart
hidoop bench mb 512 &>> $bobs_results

$HIDOOP_HOME/bench/change-bs.sh 1024000000
hidoop restart
hidoop bench mb 512 &>> $bobs_results

$HIDOOP_HOME/bench/change-bs.sh 2048000000
hidoop restart
hidoop bench mb 512 &>> $bobs_results