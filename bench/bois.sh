source ~/.bashrc

#  On fixe la taille de blocs
./change-bs 128000000

# On fixe la taille du cluster
cp 1w.xml $HIDOOP_HOME/config/core-site.xml

# On fait varier la taille des fichiers d'entrée
touch bois_results
hidoop bench mb 64 &>> bois_results
hidoop bench mb 128 &>> bois_results
hidoop bench mb 256 &>> bois_results
hidoop bench mb 512 &>> bois_results
hidoop bench gb 1 &>> bois_results
hidoop bench gb 2 &>> bois_results
hidoop bench gb 5 &>> bois_results
hidoop bench gb 10 &>> bois_results