package application;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import map.MapReduce;
import ordo.Job;
import formats.Format;
import formats.FormatReader;
import formats.FormatWriter;
import formats.KV;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class QuasiMonteCarloKV implements MapReduce {
	private static final long serialVersionUID = 1L;

    /** 2-dimensional Halton sequence {H(i)},
     * where H(i) is a 2-dimensional point and i >= 1 is the index.
     * Halton sequence is used to generate sample points for Pi estimation. 
     */
    private static class HaltonSequence {
        /** Bases */
        static final int[] P = {2, 3}; 
        /** Maximum number of digits allowed */
        static final int[] K = {63, 40}; 
    
        private long index;
        private double[] x;
        private double[][] q;
        private int[][] d;
    
        /** Initialize to H(startindex),
         * so the sequence begins with H(startindex+1).
         */
        HaltonSequence(long startindex) {
          index = startindex;
          x = new double[K.length];
          q = new double[K.length][];
          d = new int[K.length][];
          for(int i = 0; i < K.length; i++) {
            q[i] = new double[K[i]];
            d[i] = new int[K[i]];
          }
    
          for(int i = 0; i < K.length; i++) {
            long k = index;
            x[i] = 0;
            
            for(int j = 0; j < K[i]; j++) {
              q[i][j] = (j == 0? 1.0: q[i][j-1])/P[i];
              d[i][j] = (int)(k % P[i]);
              k = (k - d[i][j])/P[i];
              x[i] += d[i][j] * q[i][j];
            }
          }
        }
    
        /** Compute next point.
         * Assume the current point is H(index).
         * Compute H(index+1).
         * 
         * @return a 2-dimensional point with coordinates in [0,1)^2
         */
        double[] nextPoint() {
          index++;
          for(int i = 0; i < K.length; i++) {
            for(int j = 0; j < K[i]; j++) {
              d[i][j]++;
              x[i] += q[i][j];
              if (d[i][j] < P[i]) {
                break;
              }
              d[i][j] = 0;
              x[i] -= (j == 0? 1.0: q[i][j-1]);
            }
          }
          return x;
        }
      }

	public void map(FormatReader reader, FormatWriter writer) {

    // Map<"1",numInside>
    // Map<"0",numOutside>
    Map<String,Long> hm = new HashMap<>();
    
    // k = offset, v = size (HaltonSequence)
    KV kv;
    
    while ((kv = reader.read()) != null) {
      long offset = Long.parseLong(kv.k);
      long size = Long.parseLong(kv.v);
      final HaltonSequence haltonsequence = new HaltonSequence(offset);
      long numInside = 0L;
      long numOutside = 0L;
  
      for(long i = 0; i < size; i++) {
        //generate points in a unit square
        final double[] point = haltonsequence.nextPoint();
  
        //count points inside/outside of the inscribed circle of the square
        final double x = point[0] - 0.5;
        final double y = point[1] - 0.5;
        if (x*x + y*y > 0.25) {
          numOutside++;
        } else {
          numInside++;
        }
        
        if (hm.containsKey("1")) hm.put("1", hm.get("1")+numInside);
				else hm.put("1", numInside);

        if (hm.containsKey("0")) hm.put("0", hm.get("0")+numOutside);
				else hm.put("0", numOutside);
			}
    }

    writer.write(new KV("1",hm.get("1").toString()));
    writer.write(new KV("0",hm.get("0").toString()));
  }
  
  
	public void reduce(FormatReader reader, FormatWriter writer) {

    long numMaps = 0;
    long numPoints = 0;

    Map<String,Long> hm = new HashMap<>();
		KV kv;
		while ((kv = reader.read()) != null) {

      numMaps++;
      numPoints += Long.parseLong(kv.v);

			if (hm.containsKey(kv.k)) 
				hm.put(kv.k, hm.get(kv.k)+Long.parseLong(kv.v));
			else 
				hm.put(kv.k, Long.parseLong(kv.v));
    }
    
    numMaps = numMaps/2;
    long numInsideTotal = hm.get("1");
    //long numOutside = hm.get("0");
  

    // calculer l'estimation de PI
    final BigDecimal numTotal
      = BigDecimal.valueOf(numMaps).multiply(BigDecimal.valueOf(numPoints));
    
    final BigDecimal piQMC = BigDecimal.valueOf(4).setScale(20)
      .multiply(BigDecimal.valueOf(numInsideTotal))
      .divide(numTotal, RoundingMode.HALF_UP);

    // ecrire la valeur estimee
    writer.write(new KV("PI", piQMC.toString()));
  }
	
	public static void main(String[] args) throws Exception {
		Job j = new Job();
        j.setInputFormat(Format.Type.LINE);
        j.setInputFname(args[0]);
        long t1 = System.currentTimeMillis();
		j.startJob(new QuasiMonteCarloKV());
		long t2 = System.currentTimeMillis();
        System.out.println("time in ms ="+(t2-t1));
        System.exit(0);
		}
}
