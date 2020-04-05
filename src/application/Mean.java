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

		writer.write(new KV());
	}
	
	public void reduce(FormatReader reader, FormatWriter writer) {
         writer.write(new KV());
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
