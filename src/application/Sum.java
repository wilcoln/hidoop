package application;
 
import java.io.*;
public class Sum {

	public static void main(String[] args) {

		try {
            long t1 = System.currentTimeMillis();

            long sum = 0L;
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(args[0])));
			while (true) {
				String l = reader.readLine();
                if (l == null) break;
                String[] list = l.split(" ");
                for (String elt : list){
				    if (!elt.equals("")) {
                        sum += ((long) Float.parseFloat(elt));
                    }
                }
            }
			File resultFile = new File("sum.out."+args[0]);
			resultFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile, false)));
			writer.write("Sum<->"+sum);
			writer.newLine();
			writer.close();
			reader.close();
            long t2 = System.currentTimeMillis();
            System.out.println("time in ms ="+(t2-t1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
