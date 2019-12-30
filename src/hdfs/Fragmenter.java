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

	public static String[] fragmenterFichier(String emplacementDuFichier, int tailleMX, String emplcmtDesFrags,
			Format.Type t) throws IOException {
		if (emplacementDuFichier.equals(""))
			throw new IOException();
		// type du fichier
		long t1 = System.currentTimeMillis();
		type = t;
		tailleMax = tailleMX;
		// emplacement des fragments
		emplacementDesFrags = emplcmtDesFrags;
		// creer l'emplacement des fragments
		creerLaDest();
		// System.out.println(System.getProperty("user.dir"));
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
		FileWriter out = new FileWriter(new File(emplcmtDesFrags,emplacementDuFichier+".frag."+ordreDuFrag));
		files.add(emplcmtDesFrags+"/"+emplacementDuFichier+".frag."+ordreDuFrag);
		int tailleFragmentAct = 0;
		while ((ligne = bufReader.readLine()) != null) {
			if (tailleFragmentAct >= tailleMax) {
				out.close();
				ordreDuFrag++;
				out = new FileWriter(new File(emplcmtDesFrags,emplacementDuFichier+".frag."+ordreDuFrag));
				files.add(emplcmtDesFrags+"/"+emplacementDuFichier+".frag."+ordreDuFrag);
				tailleFragmentAct = 0;
			}
			out.write(ligne+"\n");
			tailleFragmentAct += (ligne + "\n").length();
		}
		out.close();

		fragments = new String[files.size()];
		int c = 0;
		for (String file : files) {
			fragments[c] = files.get(c);
			c++;
		}
		System.out.println(" ... OK \n   " + fragments.length + " fragments crées");
		System.out.println("Temps de Fragmentation : "+(System.currentTimeMillis() - t1) +" ms");
		return fragments;
	}

	private static void creerLaDest() {
		destination = new File(emplacementDesFrags);
		if (destination.exists())
			destination.delete();
		destination.mkdir();
	}

	public static void toFichier(File destFile, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(destFile));
		writer.write(content);
		writer.flush();
		writer.close();
		writer = null;
	}

}
