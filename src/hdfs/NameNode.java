package hdfs;


import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import config.Config;
import ordo.MapWorker;
import ordo.MapWorkerIt;
import utils.Log;
import utils.Node;
import utils.Pair;
import utils.Utils;

public class NameNode extends UnicastRemoteObject implements NameNodeIt {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, ArrayList<Pair<Integer, Node>>> filesIndex = new HashMap<>();
	public NameNode() throws RemoteException{}
	public HashMap<String, ArrayList<Pair<Integer, Node>>> getFilesIndex() throws RemoteException {
		return this.filesIndex;
	}

	@Override
	public ArrayList<Pair<Integer, Node>> get(String fname) throws RemoteException {
		return filesIndex.get(fname);
	}

	@Override
	public void put(String fname, ArrayList<Pair<Integer, Node>> fragsAndNode) throws RemoteException {
		filesIndex.put(fname, fragsAndNode);
	}

	@Override
	public void remove(String fname) throws RemoteException {
		filesIndex.remove(fname);
	}

	@Override
	public String filesIndex2String() throws RemoteException {
		return Utils.filesIndex2String(filesIndex);
	}

	public static void main(String[] args) {
		try {
			Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
			NameNodeIt obj = new NameNode();
			String hostname = Config.master.getHostname();
			String nameNodeUrl = "//" + hostname + ":" + Config.RMIREGISTRY_PORT + "/NameNode";
			System.setProperty("java.rmi.server.hostname", hostname);
			Naming.rebind(nameNodeUrl, obj);
			Log.s("NameNode", "NameNode " + "enregistré à " + nameNodeUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
