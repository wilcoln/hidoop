package ordo;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Callback extends Remote, Serializable {
    public void notifyMapsFinished(String workerHostname) throws RemoteException;
}
