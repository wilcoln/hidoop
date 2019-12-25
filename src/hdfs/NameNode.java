package hdfs;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Node;
import utils.Pair;

public class NameNode implements Serializable{
	private HashMap<String, ArrayList<Pair<Integer, Node>>> filesIndex = new HashMap<>();
	public NameNode() throws RemoteException{}
	public HashMap<String, ArrayList<Pair<Integer, Node>>> getFilesIndex() throws RemoteException {
		return this.filesIndex;
	} 
}
