package hdfs;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.swing.Spring;

public interface ConsoleItf extends Remote {
	public void println(String s) throws RemoteException;


}
