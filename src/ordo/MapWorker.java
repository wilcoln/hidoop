package ordo;

import config.Config;
import formats.Format;
import map.Mapper;
import utils.Utils;

import java.net.InetAddress;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class MapWorker extends UnicastRemoteObject implements MapWorkerIt {
    
    private static final long serialVersionUID = -5740398158577709325L;
    public MapWorker() throws RemoteException {
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, Callback cb) throws RemoteException{
        reader.open(Format.OpenMode.R);
        writer.open(Format.OpenMode.W);
        m.map(reader, writer);
        Utils.fetchHdfsClient().HdfsWrite(writer.getType(), writer.getFname(), 1);
        Utils.deleteFromLocal(writer.getFname());
        cb.onMapFinished();
    }

    public static void main(String[] args) {
        try {
            Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
            MapWorkerIt obj = new MapWorker();
            String workerUrl = "//" + InetAddress.getLocalHost().getHostName() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
            Naming.rebind(workerUrl, obj);
            System.out.println("Map Worker Impl " + "bound in registry at " + workerUrl );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
