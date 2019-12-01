package hdfs;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface HdfsServerIt extends Remote {
	public void execCmd(Commande cmd,String fichier,long taillefrag) throws RemoteException, IOException, InterruptedException;
}
