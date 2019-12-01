package hdfs;

import formats.Format;
import utils.Node;
import utils.Pair;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;


public interface HdfsClientIt extends Remote {

	HashMap<String, ArrayList<Pair<Integer, Node>>> getFilesIndex() throws RemoteException;
	void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor) throws RemoteException;
	void HdfsRead(String hdfsFname, String localFSDestFname) throws RemoteException;
	void HdfsDelete(String hdfsFname) throws RemoteException;

}
