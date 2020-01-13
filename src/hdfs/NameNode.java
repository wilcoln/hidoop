package hdfs;


import java.io.*;
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
	private static final String pathToFilesIndex = Config.STORAGE_PATH + "/.files_index";
	private HashMap<String, ArrayList<Pair<Integer, ClusterNode>>> filesIndex = new HashMap<>();

	public NameNode() throws RemoteException{
		loadFilesIndex();
	}

	@Override
	public ArrayList<Pair<Integer, ClusterNode>> get(String fname) throws RemoteException {
		return filesIndex.get(fname);
	}

	@Override
	public void put(String fname, ArrayList<Pair<Integer, ClusterNode>> fragsAndNode) throws RemoteException {
		filesIndex.put(fname, fragsAndNode);
		saveFilesIndex();
	}

	@Override
	public void remove(String fname) throws RemoteException {
		filesIndex.remove(fname);
		saveFilesIndex();
	}

	@Override
	public String lsFile(String hdfsFname) throws RemoteException {
		if(filesIndex.containsKey(hdfsFname)){
			StringBuilder result = new StringBuilder(hdfsFname + " => [\n");
			for (Pair<Integer, ClusterNode> fragAndNode : filesIndex.get(hdfsFname)) {
				result.append("\t(").append(fragAndNode.getKey()).append(", ").append(fragAndNode.getValue().getHostname()).append(");\n");
			}
			result.append("]\n");
			return result.toString();
		}
		return "File not found";
	}

	private void saveFilesIndex() {
		// TODO: sauve l'index des fichiers dans Config.DATA_PATH/.files_index

		// serialize the object
		try {
			FileOutputStream f = new FileOutputStream(new File(pathToFilesIndex));
			ObjectOutputStream o = new ObjectOutputStream(f);

			// Write filesIndex to file
			o.writeObject(filesIndex);

			o.close();
			f.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadFilesIndex() {
		// TODO: charge l'index des fichiers depuis Config.DATA_PATH/.files_index

		// deserialize the object
		if((new File(pathToFilesIndex)).exists()){
			try {
				FileInputStream fi = new FileInputStream(new File(pathToFilesIndex));
				ObjectInputStream oi = new ObjectInputStream(fi);

				// Read fileIndex
				filesIndex = (HashMap<String, ArrayList<Pair<Integer, ClusterNode>>>) oi.readObject();

				oi.close();
				fi.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
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
		Fragmenter.creerLaDest(Config.STORAGE_PATH +"/");
		String ipAddress = Config.MASTER.getIpAddress();
		String nameNodeUrl = "//" + ipAddress + ":" + Config.RMIREGISTRY_PORT + "/NameNode";
		System.setProperty("java.rmi.server.hostname", ipAddress);
		Naming.rebind(nameNodeUrl, obj);

	}
}
