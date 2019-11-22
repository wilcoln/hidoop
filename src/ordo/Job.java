package ordo;

import config.Project;
import formats.*;
import map.MapReduce;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import static formats.Format.Type.LINE;

public class Job extends UnicastRemoteObject implements JobInterface, Callback {
    private static ArrayList<String> workersReady = new ArrayList<>();
    private Format.Type inputFormat;
    private String inputFname;
    private MapReduce mapReduce;
    private Format reader;
    private Format writer;
    public Job() throws RemoteException {
    }
    @Override
    public void setInputFormat(Format.Type ft) {
        this.inputFormat = ft;
    }

    @Override
    public void setInputFname(String fname) {
        this.inputFname = fname;
    }

    @Override
    public void startJob(MapReduce mr) {
        this.mapReduce = mr;
        try {
            this.reader = (inputFormat == Format.Type.LINE)? new LineFormat(inputFname) : new KVFormat(inputFname);
            this.writer = new KVFormat(inputFname+ "--res-part");
            this.reader.open(Format.OpenMode.R);
            this.writer.open(Format.OpenMode.W);
            // Lancement des démons
            for(String[] workerInfo: Project.WORKERS) {
                String workerUrl = "//" + workerInfo[1] + ":" + Project.RMIREGISTRY_PORT + "/" + workerInfo[0];
                HidoopWorker worker = (HidoopWorker) Naming.lookup(workerUrl);
                worker.runMap(mr, reader, writer, new Job());
                //worker.test(new Job());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void notifyMapsFinished(String workerHostname) throws RemoteException {
        workersReady.add(workerHostname);
        System.out.println("NOTIFICATION REÇU : " + workerHostname + " a terminé son map");
        if(workersReady.size() == Project.WORKERS.length){
            // get back
           // mapReduce.reduce(writer, readerWriter);
            System.out.println("Tous les maps sont terminés!");
        }

    }
}
