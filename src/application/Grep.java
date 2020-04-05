package application;
 
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class Grep {

	public static void main(String[] args) {

		try {
            long t1 = System.currentTimeMillis();

			Map<String,Integer> hm = new HashMap<>();
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(args[0])));
			File resultFile = new File("grep.out."+args[0]);
			resultFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile, false)));
            int lineNb = 1;
			while (true) {
				String l = lnr.readLine();
				if (l == null) break;
				StringTokenizer st = new StringTokenizer(l);
				while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    if (true){
					    writer.write(lineNb+"<->"+l);
                        writer.newLine();
                    }
                }
                lineNb++;
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
