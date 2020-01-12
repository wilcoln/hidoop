package hdfs;


import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import config.Config;
import utils.*;

public class NameNode extends UnicastRemoteObject implements NameNodeIt {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, ArrayList<Pair<Integer, ClusterNode>>> filesIndex = new HashMap<>();
	public NameNode() throws RemoteException{}
	public HashMap<String, ArrayList<Pair<Integer, ClusterNode>>> getFilesIndex() throws RemoteException {
		return this.filesIndex;
	}

	@Override
	public ArrayList<Pair<Integer, ClusterNode>> get(String fname) throws RemoteException {
		return filesIndex.get(fname);
	}

	@Override
	public void put(String fname, ArrayList<Pair<Integer, ClusterNode>> fragsAndNode) throws RemoteException {
		filesIndex.put(fname, fragsAndNode);
	}

	@Override
	public void remove(String fname) throws RemoteException {
		filesIndex.remove(fname);
	}

	@Override
	public String lsFile(String hdfsFname) throws RemoteException {
		if(filesIndex.containsKey(hdfsFname))
			return Utils.lsHdfsFile(hdfsFname, filesIndex.get(hdfsFname));
		return "File not found";
	}
	@Override
	public String lsFiles() throws RemoteException {
		StringBuilder result = new StringBuilder();
		for (String s : filesIndex.keySet()) {
			result.append(lsFile(s));
		}
		return result.toString();
	}

	public static void main(String[] args) throws Exception {
		Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
		NameNodeIt obj = new NameNode();
		Fragmenter.creerLaDest("../data/");
		String ipAddress = Config.MASTER.getIpAddress();
		String nameNodeUrl = "//" + ipAddress + ":" + Config.RMIREGISTRY_PORT + "/NameNode";
		System.setProperty("java.rmi.server.hostname", ipAddress);
		Naming.rebind(nameNodeUrl, obj);
	}
}
