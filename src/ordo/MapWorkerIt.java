package ordo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import map.Mapper;
import formats.Format;

public interface MapWorkerIt extends Remote {
    void runMap (Mapper m, Format reader, Format writer, CallbackIt cb) throws RemoteException;
}
