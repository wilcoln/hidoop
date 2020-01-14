package hdfs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

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
		StringBuffer contenuDuFrag = new StringBuffer();
		String ligne;
		int ordreDuFrag = 0;
		// Version 2
		FileWriter out = new FileWriter(new File(emplcmtDesFrags,nomDuFichier+".frag."+ordreDuFrag));
		files.add(emplcmtDesFrags+"/"+nomDuFichier+".frag."+ordreDuFrag);
		int tailleFragmentAct = 0;
		String motRestant = "";
		String ligneAAjouter;
		while ((ligne = bufReader.readLine()) != null) {
			ligneAAjouter = ligne;
			if (t==Format.Type.KV) {
				if (!estKeyValue(dernierNonVide(ligne))){
					ligneAAjouter = ligneSansDernierMot(ligne);
				}
			}
			if (tailleFragmentAct >= tailleMax) {
				out.close();
				ordreDuFrag++;
				out = new FileWriter(new File(emplcmtDesFrags,nomDuFichier+".frag."+ordreDuFrag));
				files.add(emplcmtDesFrags+"/"+nomDuFichier+".frag."+ordreDuFrag);
				tailleFragmentAct = 0;
			}
			out.write(motRestant+ligneAAjouter+"\n");
			tailleFragmentAct += (ligne + "\n").length();
			if (t==Format.Type.KV && !estKeyValue(dernierNonVide(ligne))) {
				motRestant = dernierNonVide(ligne);
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
	public static String dernierNonVide(String ligne) {
		String [] list = ligne.split(" ");
		for (int i = list.length-1; i >= 0; i--) {
			if (!list[i].equals("")) {
				return list[i];
			}
		}
		return "";
	}
	public static String ligneSansDernierMot(String ligne) {
		String[] list = ligne.split(" ");
		String sortie = "";
		for (int i = 0;i<list.length-2;i++) {
			sortie = sortie + list[i]+" ";
		}
		sortie = list.length >=2 ? sortie + list[list.length-2] : sortie;
		return sortie;
	}
	public static void toFichier(File destFile, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(destFile));
		writer.write(content);
		writer.flush();
		writer.close();
		writer = null;
	}

}
