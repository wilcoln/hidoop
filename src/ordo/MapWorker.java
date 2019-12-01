package ordo;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import map.Mapper;
import formats.Format;

public interface MapWorker extends Remote {
    void runMap (Mapper m, Format reader, Format writer, Callback cb) throws RemoteException, MalformedURLException, NotBoundException;
}
