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

public class Mean implements MapReduce {
	private static final long serialVersionUID = 1L;

	// MapReduce program that computes word counts
	public void map(FormatReader reader, FormatWriter writer) {
		long sum = 0L;
		int nbElem = 0;
		KV kv;
		while ((kv = reader.read()) != null) {
			StringTokenizer st = new StringTokenizer(kv.v);
			while (st.hasMoreTokens()) {
				String tok = st.nextToken();
				if (!tok.equals("")) {
					sum += ((long) Float.parseFloat(tok));
					nbElem++;
				}
			}
		}
		writer.write(new KV(""+nbElem,sum+""));
	}
	
	public void reduce(FormatReader reader, FormatWriter writer) {
		KV kv;
		long sum = 0L;
		int nbElem = 0;
		while ((kv = reader.read()) != null) {
			if (!kv.v.equals("")){
				sum += Float.parseFloat(kv.v);
				nbElem += Integer.parseInt(kv.k);
			}
		}
		nbElem = nbElem==0? 0 : nbElem;
		writer.write(new KV("Mean",sum/nbElem+""));
	}
	
	public static void main(String[] args) throws Exception {
		Job j = new Job();
        j.setInputFormat(Format.Type.LINE);
        j.setInputFname(args[0]);
        long t1 = System.currentTimeMillis();
		j.startJob(new Mean());
		long t2 = System.currentTimeMillis();
        System.out.println("time in ms ="+(t2-t1));
        System.exit(0);

		}
}
