package ordo;

import config.Project;
import formats.Format;
import map.Mapper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class HidoopWorkerImpl extends UnicastRemoteObject implements HidoopWorker {
    public HidoopWorkerImpl() throws RemoteException {
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, Callback cb) throws RemoteException {

    }

    @Override
    public void test(Callback cb) throws RemoteException {
        try {
            for (int i = 0; i < 100 ; i++) {
                System.out.println(i);
            }
            String workerHostname = InetAddress.getLocalHost().getHostName();
            System.out.println(workerHostname);
            System.out.println(cb);
            cb.notifyMapsFinished(workerHostname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            HidoopWorker obj = new HidoopWorkerImpl();
            //LocateRegistry.createRegistry(Project.RMIREGISTRY_PORT);
            String workerHostname = InetAddress.getLocalHost().getHostName();
            Naming.rebind("//" + InetAddress.getLocalHost().getHostAddress() + ":" + Project.RMIREGISTRY_PORT + "/" + workerHostname, obj);
            System.out.println("HidoopWorker Impl" + "bound in registry");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
