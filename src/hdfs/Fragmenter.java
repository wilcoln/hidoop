package hdfs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import formats.Format;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Fragmenter {
	private static File destination;
	private static Format.Type type;
	private static long tailleMax;
	private static String emplacementDesFrags;

	public static File[] fragmenterFichier(String emplacementDuFichier, int tailleMX, String emplcmtDesFrags,
			Format.Type t) throws IOException {
		if (emplacementDuFichier.equals(""))
			throw new IOException();
		// type du fichier
		type = t;
		tailleMax = tailleMX;
		// emplacement des fragments
		emplacementDesFrags = emplcmtDesFrags;
		//creer l'emplacement des fragments
		creerLaDest();
		// System.out.println(System.getProperty("user.dir"));
		// listes des fragements
		File[] fragments;
		List<File> files = new ArrayList<File>();
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
		// premier fragment
		File fragActuel = Fragmenter.creerUnFragment(nomDuFichier, ordreDuFrag++);
		while ((ligne = bufReader.readLine()) != null) {
			// ajouter la line au fragment actuel
			String[] listeMot = ligne.split(" ");
			for (int j = 0; j < listeMot.length - 1; j++) {
				// si la taille max pour fragment est atteinte
				if (contenuDuFrag.length() >= tailleMax) {
					// alors ecrire le contenu du contenuDuFrag dans le fragment
					Fragmenter.toFichier(fragActuel, contenuDuFrag.toString());
					// ajouter le fragment à la liste
					files.add(fragActuel);
					// ensuite creer un nouveau fragment
					contenuDuFrag = new StringBuffer();
					fragActuel = Fragmenter.creerUnFragment(nomDuFichier, ordreDuFrag++);
				}
				contenuDuFrag.append(listeMot[j] + " ");
			}
			if (listeMot.length != 0)
				contenuDuFrag.append(listeMot[listeMot.length - 1]);
			contenuDuFrag.append("\n");
		}
		Fragmenter.toFichier(fragActuel, contenuDuFrag.toString());
		files.add(fragActuel);
		fragments = new File[files.size()];
		int c = 0;
		for (File file : files) {
			fragments[c] = files.get(c);
			c++;
		}
		System.out.println(" ... OK \n   " + fragments.length + " fragments crées");
		return fragments;
	}

	private static void creerLaDest() {
		destination = new File(emplacementDesFrags);
		if (destination.exists())
			destination.delete();
		destination.mkdir();
	}

	private static File creerUnFragment(String nomDuFichier, int indice) throws IOException {
		File frag = File.createTempFile(nomDuFichier, indice + "", destination);
		return frag;
	}

	public static void toFichier(File destFile, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(destFile));
		writer.write(content);
		writer.flush();
		writer.close();
		writer = null;
	}

	public static void main(String[] args) {
		try {
			// 1024 = 1ko
			System.out.println(Fragmenter.fragmenterFichier("file.txt", 1024, "fragments", Format.Type.LINE).length
					+ " fichiers generés");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
