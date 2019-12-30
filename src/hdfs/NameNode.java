package hdfs;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Node;
import utils.Pair;

public class NameNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, ArrayList<Pair<Integer, Node>>> filesIndex = new HashMap<>();
	public NameNode() throws RemoteException{}
	public HashMap<String, ArrayList<Pair<Integer, Node>>> getFilesIndex() throws RemoteException {
		return this.filesIndex;
	} 
}
