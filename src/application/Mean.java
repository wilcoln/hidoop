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
		writer.write(reader.read());
	}
	
	public void reduce(FormatReader reader, FormatWriter writer) {
		KV kv;
		long mean = 0L;
		int nbElem = 0;
		while ((kv = reader.read()) != null) {
			if (!kv.v.equals("")){
				mean += Float.parseFloat(kv.v);
				nbElem++;
			}
		}
		nbElem = nbElem==0 ? 1 : nbElem;
		writer.write(new KV("Mean",mean/nbElem+""));
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
