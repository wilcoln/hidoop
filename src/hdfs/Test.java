package hdfs;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException; 

public class Test {
	public static void main(String[] args) {
		long l= 100L;
		try {
			System.out.println(InetAddress.getLocalHost()+": "+InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
 