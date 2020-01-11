package ordo;

import config.Config;
import formats.*;
import hdfs.HdfsClient;
import hdfs.HdfsClientIt;
import map.MapReduce;
import utils.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Job implements JobIt {

    private int remainingFragments = 0;
    private Format.Type inputFormat;
    private String inputFname;
    private MapReduce mapReduce;
    private Format reader;
    private Format writer;
    private HdfsClientIt hdfsClient;
    private ArrayList<Pair<Integer, ClusterNode>> fileFragNodePairs;
    private Callback mapCompletedCb;
    private Semaphore allMapsCompletedSem;

    public Job() throws RemoteException{
        mapCompletedCb = new Callback(this);
        allMapsCompletedSem = new Semaphore(0);
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
    public void startJob(MapReduce mr) throws Exception {
            mapReduce = mr;
            hdfsClient = new HdfsClient();
            hdfsClient.HdfsWrite(inputFormat, inputFname, Config.REP_FACTOR);
            fileFragNodePairs = hdfsClient.getNameNode().get(inputFname);
            startMaps();  // Lancement des maps sur les fragments
            waitForMapsCompletion(); // Attente de la terminaison des maps
            mergeMapsResults();
            startReduce(); // Lancement du reduce
            Log.s("Job", ConsoleColors.GREEN_UNDERLINED+ "Terminé, fichier output -> " + (inputFname + "-reduce"));
    }

    private void startMaps() throws Exception {
        Log.i("Job", "Lancement des maps...");
        int numberFragments = fileFragNodePairs.size();
        remainingFragments = numberFragments;
        for(Pair<Integer, ClusterNode> fragAndNode: fileFragNodePairs) {
            String fragmentName = inputFname + ".frag." + fragAndNode.getKey();
            reader = (inputFormat == Format.Type.LINE)? new LineFormat(fragmentName) : new KVFormat(fragmentName);
            writer = new KVFormat(inputFname + "-map" + ".frag." + fragAndNode.getKey());
            Log.i("Job", "Lancement d'un map sur le noeud " + fragAndNode.getValue().getHostname());
            String workerUrl = "//" + fragAndNode.getValue().getHostname() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
            MapWorkerIt worker = (MapWorkerIt) Naming.lookup(workerUrl);
            worker.runMap(mapReduce, reader, writer, mapCompletedCb);
            Log.s("Job", "Map lancé sur le noeud " + fragAndNode.getValue().getHostname());
        }
        Log.s("Job", "Tous les maps sont lancés");
    }

    private void startReduce() throws RemoteException {
        Log.i("Job", "Lancement du reduce...");
        reader = new KVFormat(inputFname + "-map");
        reader.open(Format.OpenMode.R);
        writer = new KVFormat(inputFname + "-reduce");
        writer.open(Format.OpenMode.W);
        mapReduce.reduce(reader, writer);
        Log.s("Job", "Succes");
    }

    private void mergeMapsResults() throws RemoteException{
        Log.i("Job", "Fusion des resultats des maps... ");
        hdfsClient.getNameNode().put((inputFname + "map"), fileFragNodePairs);
        hdfsClient.HdfsRead(inputFname + "-map", inputFname + "-map");
        Log.s("Job", "Succes");
    }

    private void waitForMapsCompletion() throws InterruptedException{
        allMapsCompletedSem.acquire();
        Log.s("Job", "Tous les maps sont terminés");
    }

    public void onMapFinished() {
        remainingFragments--;
        if(remainingFragments == 0)
            allMapsCompletedSem.release();
        Log.w("Job", "un map terminé " + remainingFragments + " restant(s)...");
    }
}
