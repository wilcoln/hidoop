package ordo;

import config.Config;
import formats.Format;
import hdfs.HdfsClientIt;
import map.Mapper;
import utils.Utils;

import java.net.InetAddress;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class MapWorker extends UnicastRemoteObject implements MapWorkerIt {
    
    private static final long serialVersionUID = -5740398158577709325L;
    private static HdfsClientIt hdfsClient;

    public MapWorker() throws RemoteException {
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, Callback cb) throws RemoteException{
        /*reader.open(Format.OpenMode.R);
        writer.open(Format.OpenMode.W);
        m.map(reader, writer);
        Utils.fetchHdfsClient().HdfsWrite(writer.getType(), writer.getFname(), 1);
        Utils.deleteFromLocal(writer.getFname());
        cb.onMapFinished();*/

        new Thread(){ 	//Creating an object of Anonymous class which extends Thread class and passing this object to the reference of Thread class.
            public void run()	//Anonymous class overriding run() method of Thread class
            {
                try {
                    reader.open(Format.OpenMode.R);
                    writer.open(Format.OpenMode.W);
                    m.map(reader, writer);
                    cb.onMapFinished();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void main(String[] args) {
        try {
            Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
            MapWorkerIt obj = new MapWorker();
            String hostname = InetAddress.getLocalHost().getHostName();
            String workerUrl = "//" + hostname + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
            System.setProperty("java.rmi.server.hostname", hostname);
            Naming.rebind(workerUrl, obj);
            System.out.println("Map Worker Impl " + "bound in registry at " + workerUrl );
            hdfsClient = Utils.fetchHdfsClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
