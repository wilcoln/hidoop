package ordo;

import config.Config;
import formats.Format;
import hdfs.HdfsClient;
import map.Mapper;
import utils.Utils;

import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class MapWorkerImpl extends UnicastRemoteObject implements MapWorker {
    public MapWorkerImpl() throws RemoteException {
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, Callback cb) throws RemoteException {
        reader.open(Format.OpenMode.R);
        writer.open(Format.OpenMode.W);
        m.map(reader, writer);
        HdfsClient.HdfsWrite(writer.getType(), writer.getFname(), 1);
        Utils.deleteLocalFile(writer.getFname());
        cb.onMapFinished();
    }

    public static void main(String[] args) {
        try {
            MapWorker obj = new MapWorkerImpl();
            LocateRegistry.createRegistry(Config.RMIREGISTRY_PORT);
            String workerHostname = InetAddress.getLocalHost().getHostName();
            Naming.rebind("//" + InetAddress.getLocalHost().getHostAddress() + ":" + Config.RMIREGISTRY_PORT + "/" + workerHostname, obj);
            System.out.println("Map Worker Impl " + "bound in registry");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
