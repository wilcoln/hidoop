package ordo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Callback extends Remote {
    public void notifyMapsFinished(String workerHostname) throws RemoteException;
}
