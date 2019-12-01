package ordo;

import config.Config;
import formats.Format;
import hdfs.HdfsClientIt;
import map.Mapper;
import utils.Utils;

import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class MapWorker extends UnicastRemoteObject implements MapWorkerIt {
    private static HdfsClientIt hdfsClient;
    public MapWorker() throws RemoteException {
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, Callback cb) throws RemoteException{
        reader.open(Format.OpenMode.R);
        writer.open(Format.OpenMode.W);
        m.map(reader, writer);
        hdfsClient.HdfsWrite(writer.getType(), writer.getFname(), 1);
        Utils.deleteFromLocal(writer.getFname());
        cb.onMapFinished();
    }

    public static void main(String[] args) {
        try {
            Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
            MapWorkerIt obj = new MapWorker();
            Naming.rebind("//" + InetAddress.getLocalHost().getHostName() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker", obj);
            System.out.println("Map Worker Impl " + "bound in registry");
            hdfsClient = (HdfsClientIt) Naming.lookup("//" + Config.master.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/HdfsClient");
            System.out.println("Connexion à //" + Config.master.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/HdfsClient");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
