package ordo;

import config.Config;
import formats.*;
import hdfs.HdfsClientIt;
import map.MapReduce;
import utils.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Job extends UnicastRemoteObject implements JobIt, Callback {

    private static final long serialVersionUID = -4401935342947416603L;
    private int remainingFragments = 0;
    private int numberFragments = 0;
    private Format.Type inputFormat;
    private String inputFname;
    private MapReduce mapReduce;
    private Format reader;
    private Format writer;
    private HdfsClientIt hdfsClient;
    private ArrayList<Pair<Integer, Node>> fileFragNodePairs;

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
        try {
            mapReduce = mr;
            hdfsClient = Utils.fetchHdfsClient(); // récupération du client Hdfs
            fileFragNodePairs = hdfsClient.getFilesIndex().get(inputFname);
            startMaps();  // Lancement des maps sur les fragments
            waitForMapsCompletion(); // Attente de la terminaison des maps
            mergeMapsResults();
            startReduce(); // Lancement du reduce
            Log.s("Job", ConsoleColors.GREEN_UNDERLINED+ "Terminé, fichier output -> " + (inputFname + "-reduce"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startMaps() throws Exception {
        Log.i("Job", "Lancement des maps...");
        numberFragments = fileFragNodePairs.size();
        remainingFragments = numberFragments;
        for(Pair<Integer, Node> fragAndNode: fileFragNodePairs) {
            String fragmentName = inputFname + ".frag." + fragAndNode.getKey();
            reader = (inputFormat == Format.Type.LINE)? new LineFormat(fragmentName) : new KVFormat(fragmentName);
            writer = new KVFormat(inputFname + "-map" + ".frag." + fragAndNode.getKey());
            Log.i("Job", "Lancement d'un map sur le noeud " + fragAndNode.getValue().getHostname());
            String workerUrl = "//" + fragAndNode.getValue().getHostname() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
            MapWorkerIt worker = (MapWorkerIt) Naming.lookup(workerUrl);
            worker.runMap(mapReduce, reader, writer, this);
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

    @Override
    public synchronized void onMapFinished() throws RemoteException {
        remainingFragments--;
        Log.w("Job", "un map terminé " + remainingFragments + " restant(s)...");
    }

    /**
     * Lis via hdfs les fichiers résultants de l'exécution de chaque map lancé par le job
     * et crée un nouveau fichier en concaténant le tout.
     * @return le nom du fichier d'agrégation créé
     */
    private void mergeMapsResults() throws RemoteException {
        Log.i("Job", "Fusion des resultats des maps... ");
        hdfsClient.getFilesIndex().put((inputFname + "map"), fileFragNodePairs);
        hdfsClient.HdfsRead(inputFname + "-map", inputFname + "-map");
        Log.s("Job", "Succes");
    }

    private void waitForMapsCompletion() {
        while (remainingFragments > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.s("Job", "Tous les maps sont terminés");
    }
}
