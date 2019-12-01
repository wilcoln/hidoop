package hdfs;

import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class NameNode extends UnicastRemoteObject implements NameNodeIt {
	private static int portRMI;
	private static int portSocket;
	private static Socket socket;
	private static ServerSocket server;
	protected NameNode() throws RemoteException {
		super();
	}
	
	public static void main(String[] args) {
		portRMI = 4040;
		portSocket = 5050;
		try {
			Registry registry = LocateRegistry.createRegistry(portRMI);
			HdfsServer obj = new HdfsServer();
			String URL = "//localhost:" + portRMI + "/NameNode";
			Naming.rebind(URL, obj);
			server = new ServerSocket(portSocket);
			socket = server.accept();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
