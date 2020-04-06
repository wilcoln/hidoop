package application;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import map.MapReduce;
import ordo.Job;
import formats.Format;
import formats.FormatReader;
import formats.FormatWriter;
import formats.KV;

public class GrepMR implements MapReduce {

    public void map(FormatReader reader, FormatWriter writer) {

        String tok;
        StringTokenizer st;
        int firstLine=1;
        String wordToFind = null;
        KV kv = reader.read();
        st = new StringTokenizer(kv.v);

        if (st.hasMoreTokens())  wordToFind = st.nextToken();
        
        if (st.hasMoreTokens()) firstLine = Integer.parseInt(st.nextToken());

        String l1 = "";
        boolean foundInLine1 = false;
        while (st.hasMoreTokens()) {
            tok = st.nextToken();
            l1 = l1 + " " + tok;
            if (tok.equals(wordToFind)){
                foundInLine1 = true;
            }
        }
        if (foundInLine1) writer.write(new KV(firstLine+"",l1));

        boolean nextline;
        while ((kv = reader.read())!=null) {
            st = new StringTokenizer(kv.v);
            nextline = false;
            while (st.hasMoreTokens() && !nextline) {
                tok = st.nextToken();
                if (tok.equals(wordToFind)){
                    // on prends en compte l'ordre des lignes dans le fichier original
                    writer.write(new KV((Integer.parseInt(kv.k)+firstLine)+"",kv.v));
                    nextline = true;
                }
            }
        }
    }   
    public void reduce(FormatReader reader, FormatWriter writer) {

        KV kv;
		while ((kv = reader.read()) != null) {
            writer.write(kv);
		}
    }
    public static void main(String[] args) throws Exception {
        Job j = new Job();
        j.setInputFormat(Format.Type.LINE);
        j.setInputFname(args[0]);
        long t1 = System.currentTimeMillis();
        j.startJob(new GrepMR());
        long t2 = System.currentTimeMillis();
        System.out.println("time in ms ="+(t2-t1));
        System.exit(0);

    }
}
