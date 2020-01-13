# Avancement 
Cette version réalise presque le travail demandé et ils reste juste des petits nettoyages 
et des raffinements qu'on n'a pas eu le temps de faire et une eventuelle interface graphique.
# Configuration
Editer le fichier config/Config.java, spécifier le master et les workers

**NB**: Pour le test, on peut spécifier un seul noeud, qui sera à la fois le master et l'unique worker.
# Lancement
```
cd bin
export PATH=$PATH:`pwd`
hidoop start
```
# Opérations d'administration
- `hidoop show-configs`

# Opérations hdfs
- `hidoop ls`
- `hidoop ls <nom_fichier>`
- `hidoop write <nom_fichier>`
- `hidoop delete <nom_fichier>`
- `hidoop read <nom_fichier> <fichier_output>` # ne marche pas !

# Opération MapReduce
- `hidoop run <Application> <fichier_input>` # l'application doit se trouver dans le package application

# Amélioration
Persistence du file index.
