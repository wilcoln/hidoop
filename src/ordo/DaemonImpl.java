package ordo;

import config.Project;
import formats.Format;
import map.Mapper;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class DaemonImpl extends UnicastRemoteObject implements Daemon {
    public DaemonImpl() throws RemoteException {
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, Daemon cb) throws RemoteException {

    }

    @Override
    public String test() throws RemoteException {
        return "Hi";
    }

    public static void main(String[] args) {
        try {
            Daemon obj = new DaemonImpl();
            //LocateRegistry.createRegistry(Project.RMIREGISTRY_PORT);
            Naming.rebind("//192.168.122.1:" + Project.RMIREGISTRY_PORT + "/mapred", obj);
            System.out.println("DaemonImpl" + "bound in registry");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
