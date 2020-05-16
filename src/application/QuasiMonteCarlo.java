package application;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import map.MapReduce;
import ordo.Job;
import formats.Format;
import formats.FormatReader;
import formats.FormatWriter;
import formats.LineFormat;
import formats.KV;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class QuasiMonteCarlo implements MapReduce {
	private static final long serialVersionUID = 1L;

	public void map(FormatReader reader, FormatWriter writer) {

		// lire le tableau de points
		long nbPoints = 0L;
		double points[][] = new double[1000000][2];
		KV kv;
		while ((kv = reader.read()) != null) {
			String[] coordonnees = kv.v.split(",");
			double xLu = Double.parseDouble(coordonnees[0]);
			double yLu = Double.parseDouble(coordonnees[1]);
			double[] pointLu = {xLu,yLu};
			points[(int) nbPoints] = pointLu;
			nbPoints++;
		}

		// compter les points a l'interieur et a l'exterieur du cercle
		long numInside = 0L;
		long numOutside = 0L;
		for (long i = 0L; i < nbPoints-1; i++) {
			double x = points[(int) i][0] - 0.5;
			double y = points[(int) i][1] - 0.5;
			if (x * x + y * y > 0.25) {
				numOutside++;
			} else {
				numInside++;
			}
		}

		// ecrire numInside et numOutside
		writer.write(new KV("1", numInside+""));
		writer.write(new KV("0", numOutside+""));

	}

	public void reduce(FormatReader reader, FormatWriter writer) {
		
		// recuperer le resultats sur les points
		long numInsideTotal = 0L;
		long numOutsideTotal = 0L;
		KV kv;
		while ((kv = reader.read()) != null) {
			String tkn = kv.k;
			long numPoints = Long.parseLong(kv.v);
			if (tkn.equals("1")) {
				numInsideTotal += numPoints;
			} else {
				numOutsideTotal += numPoints;
			}
		}		
		long total = numInsideTotal + numOutsideTotal;
		BigDecimal numTotal = BigDecimal.valueOf(total);

		// estimer la valeur de PI
		final BigDecimal piQMC = BigDecimal.valueOf(4).setScale(20).multiply(BigDecimal.valueOf(numInsideTotal))
				.divide(numTotal, RoundingMode.HALF_UP);

		// ecrire la valeur estimee
		writer.write(new KV("PI", piQMC.toString()));

	}

	public static void main(String[] args) throws Exception {
		Job j = new Job();
		j.setInputFormat(Format.Type.LINE);
		j.setInputFname(args[0]);
		long t1 = System.currentTimeMillis();
		j.startJob(new QuasiMonteCarlo());
		long t2 = System.currentTimeMillis();
		System.out.println("time in ms =" + (t2 - t1));
		System.exit(0);
	}
}
