package hdfs;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Console extends UnicastRemoteObject implements ConsoleItf {

	public Console() throws RemoteException {}
	public void println (String s) throws RemoteException {
		System.out.println(s);
	}

}