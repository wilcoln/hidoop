package hdfs;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;


public interface HdfsClientIt extends Remote {
	
	public HashMap<String, ArrayList<Pair<Integer, String>>> filesIndex();

}
