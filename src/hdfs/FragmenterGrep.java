package hdfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import utils.Utils;
public class FragmenterGrep extends Fragmenter{
   
    public FragmenterGrep(){
        super();
    }
	public String[] fragmenterFichier(String emplacementDuFichier, int tailleMax, String emplacementDesFrags) throws IOException {

        if (emplacementDuFichier.equals(""))
            throw new IOException();

        // creer l'emplacement des fragments
        Utils.createPath(emplacementDesFrags);

        // listes des fragements
        String[] fragments;
        List<String> files = new ArrayList<String>();

        // ouvrir le fichier pour la lecture
        BufferedReader bufReader = new BufferedReader(new FileReader(emplacementDuFichier));

        //enlever le path et ne garder que le nom du fichier (ex : path/vers/le/fichier.ext => fichier.ext)
        String[] l = emplacementDuFichier.split("/");
        String nomDuFichier = l[l.length - 1];

        System.out.print("Fragmentation du fichier: " + nomDuFichier);

        // conteneur temporaire du fragment
        String ligne;

        // Spécial type grep
        int lineNumber = 0;
        String wordToFind = null;

        // le premier fragment crée
        int ordreDuFrag = 0;
        FileWriter out = new FileWriter(new File(emplacementDesFrags,nomDuFichier+".frag."+ordreDuFrag));
        // ajouter le premier fragment à la liste des fichiers 
        files.add(emplacementDesFrags+"/"+nomDuFichier+".frag."+ordreDuFrag);
        // initialiser la taille des données insérées dans le fragment
        int tailleFragmentAct = 0;

        // special type GREP
        // récupérer le mot à trouver
        String l1 = "";
        if((ligne = bufReader.readLine()) != null){
            lineNumber++;
            StringTokenizer st = new StringTokenizer(ligne);
            if (st.hasMoreTokens())  wordToFind = st.nextToken();
            l1 = wordToFind + " " + lineNumber;
            while (st.hasMoreTokens()) {
                l1 = l1 + " " + st.nextToken();
            }   
        }
        // Ecrire la ligne lue avec une eventuelle clé perdue de la ligne précedente
        out.write(l1+"\n");
        
        // Faire la m-a-j de la taille actuelle du fragment
        tailleFragmentAct += (l1 + "\n").length(); 

        while ((ligne = bufReader.readLine()) != null) {
            lineNumber++;

            // Si on depasse la taille max, fermer ce fragment et lancer un autre
            if (tailleFragmentAct >= tailleMax) {
                out.close();
                ordreDuFrag++;
                // on crée un nouveau fragment
                out = new FileWriter(new File(emplacementDesFrags,nomDuFichier+".frag."+ordreDuFrag));
                // on l'ajoute à la liste des fragments
                files.add(emplacementDesFrags+"/"+nomDuFichier+".frag."+ordreDuFrag);
                // on reinitialiser la taille des données insérées dans le fragment 
                tailleFragmentAct = 0;

                // Special type GREP 
                // inserer le mot a chercher et la ligne du debut du fragment
                out.write(wordToFind + " " + lineNumber+" ");
                tailleFragmentAct += (wordToFind + " " + lineNumber+" ").length();
            }
            // Ecrire la ligne lue avec une eventuelle clé perdue de la ligne précedente
            out.write(ligne+"\n");

            // Faire la m-a-j de la taille actuelle du fragment
            tailleFragmentAct += (ligne + "\n").length();
        
        }
        out.close();
        bufReader.close();
        fragments = new String[files.size()];
        int c = 0;
        for (String file : files) {
            fragments[c] = file;
            c++;
        }
        System.out.println(" ... OK \n   " + fragments.length + " fragments crées");
        return fragments;
    }

}