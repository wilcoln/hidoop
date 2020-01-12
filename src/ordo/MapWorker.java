package ordo;

import config.Config;
import formats.Format;
import hdfs.HdfsClientIt;
import map.Mapper;
import utils.Log;
import utils.Utils;

import java.net.InetAddress;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class MapWorker extends UnicastRemoteObject implements MapWorkerIt {
    
    private static final long serialVersionUID = -5740398158577709325L;

    public MapWorker() throws RemoteException {
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, CallbackIt cb) throws RemoteException{
        new Thread(){ 	//Creating an object of Anonymous class which extends Thread class and passing this object to the reference of Thread class.
            public void run()	//Anonymous class overriding run() method of Thread class
            {
                try {
                    reader.open(Format.OpenMode.R);
                    writer.open(Format.OpenMode.W);
                    Log.i("MapWorker", "Lancement du map sur le fragment " + reader.getFname() + "... ");
                    m.map(reader, writer);
                    Log.s("MapWorker", "Un Map terminé -> " + writer.getFname());
                    Log.i("MapWorker", "Envoie notification map terminé... ");
                    cb.onMapFinished();
                    Log.s("MapWorker", "Notification envoyée");
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
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            String workerUrl = "//" + ipAddress + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
            System.setProperty("java.rmi.server.hostname", ipAddress);
            Naming.rebind(workerUrl, obj);
            } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
