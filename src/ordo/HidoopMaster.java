package ordo;

import config.Project;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class HidoopMaster extends UnicastRemoteObject implements Callback {

    private static ArrayList<String> workersReady = new ArrayList<>();

    private HidoopMaster() throws RemoteException {
    }
    public static void main(String[] args){
        try {
           for(String workerHostname: Project.WORKERS) {
               HidoopWorker obj = (HidoopWorker) Naming.lookup("//192.168.122.1:" + Project.RMIREGISTRY_PORT + "/" + workerHostname);
              //obj.runMap()....
               System.out.println(obj.test(new HidoopMaster()));
           }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void notifyMapsFinished(String workerHostname) throws RemoteException {
        workersReady.add(workerHostname);
        System.out.println(workerHostname + " a terminé son map");
        if(workersReady.size() == Project.WORKERS.length)
            System.out.println("Maps terminés! Lanchement du reducer");
    }
}
