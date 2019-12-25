package hdfs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Test2 {
	public static void decouperFichier(File fichier, File path) throws IOException {
		Scanner in = new Scanner(fichier);
		FileWriter out = null;
		while (in.hasNextLine()) {
			String line = in.nextLine();
			if (line.startsWith("")) {
				if (out != null)
					out.close();
				out = new FileWriter(new File(path, line + ".txt"));
				System.out.println(line+".txt");
			}
			if (out != null) {
				out.write(line);
				out.write(System.lineSeparator());
			}
		}
		if (out != null)
			out.close();
		in.close();
	}

	public static void main(String[] args) throws IOException {
		decouperFichier(new File("/home/relmo/Bureau/relmonta/2A/SC/hidoo/file.line"), new File("./"));
	}
}