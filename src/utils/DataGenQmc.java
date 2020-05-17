package utils;
import java.io.File;  // Import the File class
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors

public class DataGenQmc {
	private static class HaltonSequence {
		/** Bases */
		static final int[] P = { 2, 3 };
		/** Maximum number of digits allowed */
		static final int[] K = { 63, 40 };

		private long index;
		private double[] x;
		private double[][] q;
		private int[][] d;

		/**
		 * Initialize to H(startindex), so the sequence begins with H(startindex+1).
		 */
		HaltonSequence(long startindex) {
			index = startindex;
			x = new double[K.length];
			q = new double[K.length][];
			d = new int[K.length][];
			for (int i = 0; i < K.length; i++) {
				q[i] = new double[K[i]];
				d[i] = new int[K[i]];
			}

			for (int i = 0; i < K.length; i++) {
				long k = index;
				x[i] = 0;

				for (int j = 0; j < K[i]; j++) {
					q[i][j] = (j == 0 ? 1.0 : q[i][j - 1]) / P[i];
					d[i][j] = (int) (k % P[i]);
					k = (k - d[i][j]) / P[i];
					x[i] += d[i][j] * q[i][j];
				}
			}
		}

		/**
		 * Compute next point. Assume the current point is H(index). Compute H(index+1).
		 * 
		 * @return a 2-dimensional point with coordinates in [0,1)^2
		 */
		double[] nextPoint() {
			index++;
			for (int i = 0; i < K.length; i++) {
				for (int j = 0; j < K[i]; j++) {
					d[i][j]++;
					x[i] += q[i][j];
					if (d[i][j] < P[i]) {
						break;
					}
					d[i][j] = 0;
					x[i] -= (j == 0 ? 1.0 : q[i][j - 1]);
				}
			}
			return x;
		}
	}

	public static void main(String[] args) {

		HaltonSequence haltonsequence = new HaltonSequence(0L);
		long nb = Long.parseLong(args[1]);
		long mb;
		if (args[0].equals("gb")) {
			mb = 30000000L;
		} else {
			mb = 30000L;
		}
		
		long nbPoints = nb * mb;
		double points[][] = new double[(int) nbPoints][2];


		File fichier = new File(args[1]+args[0]+".txt");
		try {
			fichier.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter writer = new FileWriter(fichier);
			for (long i = 0L; i < nbPoints; i++) {
				double[] point = haltonsequence.nextPoint();
				points[(int) i] = point;
				double x = points[(int) i][0];
				double y = points[(int) i][1];
				writer.write(x + "," + y + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
