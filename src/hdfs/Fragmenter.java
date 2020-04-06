package hdfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import formats.KV;

public abstract class Fragmenter {
	public Fragmenter() {

	}
	public abstract String[] fragmenterFichier(String emplacementDuFichier, int tailleMax, String emplacementDesFrags) throws IOException;
	
	// Verifier qu'un mot a la bonne format d'un KV 
	public static boolean IsKeyValue (String mot){
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
	public static String LastWord(String ligne) {
		String [] list = ligne.split(" ");
		for (int i = list.length-1; i >= 0; i--) {
			if (!list[i].equals("")) {
				return list[i];
			}
		}
		return "";
	}
	// enlever le dernier mot d'une ligne
	public static String LineWithoutLastWord(String ligne) {
		String[] list = ligne.split(" ");
		String sortie = "";
		for (int i = 0;i<list.length-2;i++) {
			sortie = sortie + list[i]+" ";
		}
		sortie = list.length >=2 ? sortie + list[list.length-2] : sortie;
		return sortie;
	}

}
