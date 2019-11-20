package ordo;

import config.Project;
import formats.Format;
import map.Mapper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class HidoopWorkerImpl extends UnicastRemoteObject implements HidoopWorker {
    public HidoopWorkerImpl() throws RemoteException {
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, Callback cb) throws RemoteException {

    }

    @Override
    public String test(Callback cb) throws RemoteException {
        try {
            for (int i = 0; i < 10000 ; i++) {
                System.out.println(i);
            }
            cb.notifyMapsFinished(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "done";
    }

    public static void main(String[] args) {
        try {
            HidoopWorker obj = new HidoopWorkerImpl();
            String workerHostname = InetAddress.getLocalHost().getHostName();
            Naming.rebind("//192.168.122.1:" + Project.RMIREGISTRY_PORT + "/" + workerHostname, obj);
            System.out.println("HidoopWorker Impl" + "bound in registry");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
