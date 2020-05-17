package ordo;

import config.Config;
import formats.*;
import hdfs.HdfsClient;
import hdfs.HdfsClientIt;
import map.MapReduce;
import utils.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Job implements JobIt {

    private int remainingFragments = 0;
    private Format.Type inputFormat;
    private String inputFname;
    private MapReduce mapReduce;
    private Format reader;
    private Format writer;
    private HdfsClientIt hdfsClient;
    private Map<Integer, List<ClusterNode>> fileFragments;
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
            fileFragments = hdfsClient.getNameNode().get(inputFname);
            startMaps();  // Lancement des maps sur les fragmentsoui
            waitForMapsCompletion(); // Attente de la terminaison des maps
            mergeMapsResults();
            startReduce(); // Lancement du reduce
            Log.s("Job", ConsoleColors.GREEN_UNDERLINED+ "Terminé, fichier output -> " + (inputFname + "-reduce"));
    }

    private void startMaps() throws Exception {
        Log.i("Job", "Lancement des maps...");
        int numberFragments = fileFragments.size();
        remainingFragments = numberFragments;
        for (int fragNo : fileFragments.keySet()) {
            boolean mapStarted = false;
            String fragmentName = inputFname + ".frag." + fragNo;
            reader = (inputFormat == Format.Type.KV)? new KVFormat(fragmentName) : new LineFormat(fragmentName);
            writer = new KVFormat(inputFname + "-map" + ".frag." + fragNo);

            ListIterator<ClusterNode> nodeIterator = fileFragments.get(fragNo).listIterator();
            while(!mapStarted && nodeIterator.hasNext()) {
            	try {
            	    ClusterNode cnode = nodeIterator.next();
            	    String workerUrl = "//" + cnode.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
                    MapWorkerIt worker = (MapWorkerIt) Naming.lookup(workerUrl);
            	    Log.i("Job", "Lancement d'un map sur le noeud " + cnode);
                    worker.runMap(mapReduce, reader, writer, mapCompletedCb);
                    Log.s("Job", "Map lancé sur le noeud " + cnode);
            	    mapStarted = true;
                } catch (Exception e) {/* Noeud Hors service, on tente le suivant */}
            }

            if(!mapStarted) {
                throw new Exception("Un map n'a pas pu être lancé");
            }
        }

        Log.s("Job", "Tous les maps sont lancés");
    }

    private void startReduce() {
        Log.i("Job", "Lancement du reduce...");
        reader = new KVFormat(inputFname + "-map");
        reader.open(Format.OpenMode.R);
        writer = new KVFormat(inputFname + "-reduce");
        writer.open(Format.OpenMode.W);
        mapReduce.reduce(reader, writer);
        Log.s("Job", "Succes");
    }

    private void mergeMapsResults() throws Exception{
        Log.i("Job", "Fusion des resultats des maps... ");
        String mapResFname = inputFname + "-map";
        hdfsClient.getNameNode().put(mapResFname, fileFragments);
        hdfsClient.HdfsRead(mapResFname, mapResFname);
        hdfsClient.HdfsDelete(mapResFname);
        Log.s("Job", "Succes");
    }

    private void waitForMapsCompletion() throws InterruptedException{
        allMapsCompletedSem.acquire();
        Log.s("Job", "Tous les maps sont terminés");
    }

    public synchronized void onMapFinished() {
        remainingFragments--;
        if(remainingFragments == 0)
            allMapsCompletedSem.release();
        Log.w("Job", "un map terminé " + remainingFragments + " restant(s)...");
    }
}
