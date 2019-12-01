package hdfs;

import formats.Format;
import utils.Node;
import utils.Pair;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;


public interface HdfsClientIt extends Remote {
	
	HashMap<String, ArrayList<Pair<Integer, Node>>> getFilesIndex();
	void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor);
	void HdfsRead(String hdfsFname, String localFSDestFname);
	void HdfsDelete(String hdfsFname);



}
