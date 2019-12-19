package ordo;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackIt extends Remote, Serializable {
    void onMapFinished() throws RemoteException;
}
