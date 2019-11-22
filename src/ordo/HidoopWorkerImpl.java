package ordo;

import config.Project;
import formats.Format;
import hdfs.HdfsClient;
import map.Mapper;

import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class HidoopWorkerImpl extends UnicastRemoteObject implements HidoopWorker {
    public HidoopWorkerImpl() throws RemoteException {
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, Callback cb) throws RemoteException {
        reader.open(Format.OpenMode.R);
        writer.open(Format.OpenMode.W);
        m.map(reader, writer);
        HdfsClient.HdfsWrite(writer.getType(), writer.getFname(), 1);
        cb.onMapFinished();
    }

    public static void main(String[] args) {
        try {
            HidoopWorker obj = new HidoopWorkerImpl();
            LocateRegistry.createRegistry(Project.RMIREGISTRY_PORT);
            String workerHostname = InetAddress.getLocalHost().getHostName();
            Naming.rebind("//" + InetAddress.getLocalHost().getHostAddress() + ":" + Project.RMIREGISTRY_PORT + "/" + workerHostname, obj);
            System.out.println("HidoopWorker Impl" + "bound in registry");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
