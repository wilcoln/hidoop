package ordo;

import config.Config;
import formats.*;
import hdfs.HdfsClientIt;
import map.MapReduce;
import utils.Node;
import utils.Pair;
import utils.Utils;

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
    private ArrayList<Pair<Integer, Node>> fragAndNodeList;

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
            fragAndNodeList = hdfsClient.getFilesIndex().get(inputFname);
            startMaps();  // Lancement des maps sur les fragments
            waitForMapsCompletion(); // Attente de la terminaison des maps
            mergeMapsResults();
            startReduce(); // Lancement du reduce
            System.out.println("Job Successful -> " + (inputFname + "-reduce") );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startMaps() throws Exception {
        System.out.println("Début des maps... ");
        numberFragments = fragAndNodeList.size();
        remainingFragments = numberFragments;
        for(Pair<Integer, Node> fragAndNode: fragAndNodeList) {
            String fragmentName = inputFname + ".frag." + fragAndNode.getKey();
            reader = (inputFormat == Format.Type.LINE)? new LineFormat(fragmentName) : new KVFormat(fragmentName);
            writer = new KVFormat(inputFname + "-map" + ".frag." + fragAndNode.getKey());
            String workerUrl = "//" + fragAndNode.getValue().getHostname() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
            MapWorkerIt worker = (MapWorkerIt) Naming.lookup(workerUrl);
            worker.runMap(mapReduce, reader, writer, this);
        }
    }

    private void startReduce() throws RemoteException {
        System.out.print("Début du reduce... ");
        reader = new KVFormat(inputFname + "-map");
        reader.open(Format.OpenMode.R);
        writer = new KVFormat(inputFname + "-reduce");
        writer.open(Format.OpenMode.W);
        mapReduce.reduce(reader, writer);
        System.out.println("Successful");
    }

    @Override
    public void onMapFinished() throws RemoteException {
        remainingFragments--;
        System.out.println("NOTIFICATION REÇUE : un map terminé " + remainingFragments + " restant(s)");
    }

    /**
     * Lis via hdfs les fichiers résultants de l'exécution de chaque map lancé par le job
     * et crée un nouveau fichier en concaténant le tout.
     * @return le nom du fichier d'agrégation créé
     */
    private void mergeMapsResults() throws RemoteException {
        System.out.print("Fusion des resultats des maps... ");
        hdfsClient.getFilesIndex().put((inputFname + "map"), fragAndNodeList);
        hdfsClient.HdfsRead(inputFname + "-map", inputFname + "-map");
        System.out.println("Successful");
    }

    private void waitForMapsCompletion() {
        while (remainingFragments > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("All maps successful");
    }
}
