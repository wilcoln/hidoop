package application;
 
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class Grep {

	public static void main(String[] args) {

		try {
            long t1 = System.currentTimeMillis();

            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(args[0])));
			File resultFile = new File("grep.out."+args[0]);
			resultFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile, false)));
			String l = lnr.readLine();
			String tok;
			StringTokenizer st;
			String wordToFind = null;
			st = new StringTokenizer(l);
			// récupérer le mot à chercher
			if (st.hasMoreTokens()){
				wordToFind = st.nextToken();
			}

			while (true) {
				while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    if (tok.equals(wordToFind)){
					    writer.write(lnr.getLineNumber()+"<->"+l);
						writer.newLine();
						continue;
                    }
				}
				l = lnr.readLine();
				if (l == null) break;
				st = new StringTokenizer(l);
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
