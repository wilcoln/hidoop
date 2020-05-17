package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Qmc {

	public static void main(String[] args) throws NumberFormatException, IOException {

		File fichier = new File(args[0]);
		FileReader reader = new FileReader(fichier);
		BufferedReader br = new BufferedReader(reader);
		String ligne;

		long numInsideTotal = 0L;
		long numOutsideTotal = 0L;

		while ((ligne = br.readLine()) != null) {
			String[] coordonnees = ligne.split(",");
			double x = Double.parseDouble(coordonnees[0]);
			double y = Double.parseDouble(coordonnees[1]);
			if (x * x + y * y > 0.25) {
				numOutsideTotal++;
			} else {
				numInsideTotal++;
			}
		}

		reader.close();

		long total = numInsideTotal + numOutsideTotal;
		BigDecimal numTotal = BigDecimal.valueOf(total);

		// estimer la valeur de PI
		final BigDecimal piQMC = BigDecimal.valueOf(16).setScale(20).multiply(BigDecimal.valueOf(numInsideTotal)).divide(numTotal, RoundingMode.HALF_UP);

		File resultFile = new File("Qmc.out."+args[0]);
		resultFile.createNewFile();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile, false)));
		writer.write("PI<->"+piQMC);
		writer.newLine();
		writer.close();
	}
}