source ~/.bashrc

#  On fixe la taille de blocs
./change-bs 128000000

# On fixe la taille du fichier d'entrée
input_size=2

# On fait varier la taille du cluster
touch bocs_results

hidoop stop
cp 1w.xml $HIDOOP_HOME/config/core-site.xml
hidoop start
hidoop bench gb $input_size &>> bocs_results

hidoop stop
cp 2w.xml $HIDOOP_HOME/config/core-site.xml
hidoop start
hidoop bench gb $input_size &>> bocs_results

hidoop stop
cp 3w.xml $HIDOOP_HOME/config/core-site.xml
hidoop start
hidoop bench gb $input_size &>> bocs_results

hidoop stop
cp 4w.xml $HIDOOP_HOME/config/core-site.xml
hidoop start
hidoop bench gb $input_size &>> bocs_results

hidoop stop
cp 5w.xml $HIDOOP_HOME/config/core-site.xml
hidoop start
hidoop bench gb $input_size &>> bocs_results