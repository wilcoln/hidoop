package ordo;

import config.Config;
import formats.*;
import hdfs.HdfsClientIt;
import map.MapReduce;
import utils.Node;
import utils.Pair;
import utils.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
        mapReduce = mr;
        hdfsClient = Utils.fetchHdfsClient(); // récupération du client Hdfs

        try {
            startMaps();  // Lancement des maps sur les fragments
            waitForMapsCompletion(); // Attente de la terminaison des maps
            startReduce(); // Lancement du reduce
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startMaps() throws Exception {
        numberFragments = hdfsClient.getFilesIndex().get(inputFname).size();
        remainingFragments = numberFragments;
        for(Pair<Integer, Node> fragmentAndNode: hdfsClient.getFilesIndex().get(inputFname)) {
            String fragmentName = inputFname + ".frag." + fragmentAndNode.getKey();
            reader = (inputFormat == Format.Type.LINE)? new LineFormat(fragmentName) : new KVFormat(fragmentName);
            writer = new KVFormat(fragmentName + "-map");
            String workerUrl = "//" + fragmentAndNode.getValue().getHostname() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
            MapWorkerIt worker = (MapWorkerIt) Naming.lookup(workerUrl);
            worker.runMap(mapReduce, reader, writer, this);
        }
    }

    private void startReduce() {
        System.out.println("Début du reduce!");
        String mergeFilename = mergeMapsResults();
        reader = new KVFormat(mergeFilename);
        reader.open(Format.OpenMode.R);
        writer = new KVFormat(inputFname + "-reduce");
        writer.open(Format.OpenMode.W);
        mapReduce.reduce(reader, writer);
        System.out.println("Reduce Terminé!");
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
    private String mergeMapsResults(){
        System.out.print("Fusion des resultats des différents map... ");
        String outputFilename = inputFname + "-map";
        try {
            OutputStream out = new FileOutputStream(outputFilename);
            byte[] buf = new byte[1024];
            // Lecture de chaque fichier résultat avec HDFS et ajout en fin de {out}
            for (int i = 0; i < numberFragments; i++) {
                String fragmentResName = inputFname + ".frag." + i + "-map";
                hdfsClient.HdfsRead(fragmentResName, fragmentResName);
                hdfsClient.HdfsDelete(fragmentResName);
                InputStream in = new FileInputStream(fragmentResName);
                int b = 0;
                while ((b = in.read(buf)) >= 0)
                    out.write(buf, 0, b);
                in.close();
                Utils.deleteFromLocal(fragmentResName);
            }
            out.close();
            System.out.println("Successful");
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputFilename;
    }

    private void waitForMapsCompletion() {
        while (remainingFragments > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
