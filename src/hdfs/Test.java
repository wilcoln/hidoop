package hdfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import utils.Utils;

public class Test {
	public static void main(String[] args) {
		long l = 100L;
		try {
			int f = (int) 333333334L;
			BufferedReader bufReader = new BufferedReader(
					new FileReader("/home/relmo/Bureau/relmonta/2A/SC/hidoo/hidoop/src/file.line"));
			String ligne;
			int s = 0;
			while ((ligne = bufReader.readLine()) != null) {
				s += ligne.getBytes().length;
			}
			System.out.println(s+":"+(new File("/home/relmo/Bureau/relmonta/2A/SC/hidoo/hidoop/src/file.line")).length());

			for (int i = 0; i < 3; i++) {
				System.out.println(Utils.splitStr("0...1234///nomFrag///..CMD_WRITE", "/")[i]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
