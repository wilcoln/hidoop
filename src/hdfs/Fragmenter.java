package hdfs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import formats.*;
import utils.Utils;

public class Fragmenter {
	private static File destination;
	private static Format.Type type;
	private static long tailleMax;
	private static String emplacementDesFrags;

	public static String[] fragmenterFichier(String emplacementDuFichier, int tailleMX, String emplcmtDesFrags,
			Format.Type t) throws IOException {
		if (emplacementDuFichier.equals(""))
			throw new IOException();
		// type du fichier
		type = t;
		tailleMax = tailleMX;
		// emplacement des fragments
		emplacementDesFrags = emplcmtDesFrags;
		// creer l'emplacement des fragments
		Utils.createPath(emplacementDesFrags);
		// listes des fragements
		String[] fragments;
		List<String> files = new ArrayList<String>();
		// ouvrir le fichier pour la lecture
		BufferedReader bufReader = new BufferedReader(new FileReader(emplacementDuFichier));
		File fichier = new File(emplacementDuFichier);
		String[] l = emplacementDuFichier.split("/");
		String nomDuFichier = l[l.length - 1];
		System.out.println(nomDuFichier);
		System.out.print("Fragmentation du fichier: " + nomDuFichier);
		// conteneur temporaire du frag
		String ligne;
		int ordreDuFrag = 0;
		FileWriter out = new FileWriter(new File(emplcmtDesFrags,nomDuFichier+".frag."+ordreDuFrag));
		files.add(emplcmtDesFrags+"/"+nomDuFichier+".frag."+ordreDuFrag);
		int tailleFragmentAct = 0;
		String motRestant = "";
		String ligneAAjouter;
		while ((ligne = bufReader.readLine()) != null) {
			ligneAAjouter = ligne;
			// Si le format est KV, Ici on verifie qu'on n'a pas de valeur sans clé c-a-d, pas de "clé<->\nvaleur" ou de "clé\n<->valeur"
			// Et si on est dans ce cas il faut garder la clé pour l'integer dans la ligne suivante
			if (t==Format.Type.KV) {
				if (!estKeyValue(dernierNonVide(ligne))){
					ligneAAjouter = ligneSansDernierMot(ligne);
				}
			}
			// Si on depasse la taille max, fermer ce fragment et lancer un autre
			if (tailleFragmentAct >= tailleMax) {
				out.close();
				ordreDuFrag++;
				out = new FileWriter(new File(emplcmtDesFrags,nomDuFichier+".frag."+ordreDuFrag));
				files.add(emplcmtDesFrags+"/"+nomDuFichier+".frag."+ordreDuFrag);
				tailleFragmentAct = 0;
			}
			// Ecrire la ligne lue avec une eventuelle clé perdue de la ligne précedente
			out.write(motRestant+ligneAAjouter+"\n");
			// Faire la m-a-j de la taille actuelle du fragment
			tailleFragmentAct += (ligne + "\n").length();
			// il faut garder la valeur perdu pour l'inserer avec la ligne suivante
			if (t==Format.Type.KV && !estKeyValue(dernierNonVide(ligne))) {
				motRestant = dernierNonVide(ligne);
			} else {
				motRestant = "";
			}

		}
		out.close();

		fragments = new String[files.size()];
		int c = 0;
		for (String file : files) {
			fragments[c] = files.get(c);
			c++;
		}
		System.out.println(" ... OK \n   " + fragments.length + " fragments crées");
		return fragments;
	}

	// Verifier qu'un mot a la bonne format d'un KV 
	public static boolean estKeyValue (String mot){
		if (mot.equals("")) {return true;}
		String [] list = mot.split(KV.SEPARATOR);
		List<String> listToReturn = new ArrayList<String>();
		for (int i = 0; i < list.length; i++) {
			if (!list[i].equals("")) {
				listToReturn.add(list[i]);
			}
		}
		if (listToReturn.size() == 2) {
			return true;
		} else if(listToReturn.size() == 0) { throw new Error();}
		else {
			try {
				
				Integer.parseInt(listToReturn.get(0));
				// normalement on tombera jamais dans ce cas si les elements des Kv sont bien ecrits
				return true;
			} catch (NumberFormatException e) {
				// la clé est dans la ligne suivante
				return false;
			}
		}
	}

	// Recuperer le dernier mot non vide d'une ligne 
	public static String dernierNonVide(String ligne) {
		String [] list = ligne.split(" ");
		for (int i = list.length-1; i >= 0; i--) {
			if (!list[i].equals("")) {
				return list[i];
			}
		}
		return "";
	}
	// enlever le dernier mot d'une ligne
	public static String ligneSansDernierMot(String ligne) {
		String[] list = ligne.split(" ");
		String sortie = "";
		for (int i = 0;i<list.length-2;i++) {
			sortie = sortie + list[i]+" ";
		}
		sortie = list.length >=2 ? sortie + list[list.length-2] : sortie;
		return sortie;
	}

}
