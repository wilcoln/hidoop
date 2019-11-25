package application;
/* Note : pour pouvoir fonctionner sans modifications, cette application suppose 
 * l'existence d'un attribut statique PATH d'une classe Projet située dans le répertoire 
 * hidoop/src/config. Cet attribut est supposé contenir le chemin d'accès au répertoire 
 * hidoop (celui qui contient le répertoire applications contenant le présent fichier)
 */
 
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import config.Config;

public class Count {

	public static void main(String[] args) {

		try {
            long t1 = System.currentTimeMillis();

			Map<String,Integer> hm = new HashMap<>();
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(Config.PATH+"data/"+args[0])));
			while (true) {
				String l = lnr.readLine();
				if (l == null) break;
				StringTokenizer st = new StringTokenizer(l);
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					hm.put(tok, hm.getOrDefault(tok, 1));
				}
			}
			File resultFile = new File(Config.OUTPUT_PATH + "/score.txt");
			resultFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile, false)));
			for (String k : hm.keySet()) {
				writer.write(k+"<->"+hm.get(k).toString());
				writer.newLine();
			}
			writer.close();
			lnr.close();
            long t2 = System.currentTimeMillis();
            System.out.println("time in ms ="+(t2-t1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
