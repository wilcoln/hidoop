package ordo;

import config.Config;
import formats.*;
import hdfs.HdfsClientIt;
import map.MapReduce;
import utils.Node;
import utils.Pair;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Job extends UnicastRemoteObject implements JobInterface, Callback {
    private static int remainingFragments = 0;
    private static int numberFragments = 0;
    private Format.Type inputFormat;
    private static String inputFname;
    private static MapReduce mapReduce;
    private Format reader;
    private Format writer;
	private HdfsClientIt client;
    
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
            client = (HdfsClientIt) Naming.lookup("//" + Config.master.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/HdfsClient");
            System.out.println("Connexion à //" + Config.master.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/HdfsClient");

			//les fragments sont nomé inputFname.frag.<numero du fragment>.<numero du noeud>
			
            // Lancement des maps sur les fragments
            numberFragments = client.getFilesIndex().get(inputFname).size();
            remainingFragments = numberFragments;
            for(Pair<Integer, Node> fragmentAndNode: client.getFilesIndex().get(inputFname)) {
                String fragmentName = inputFname + ".frag." + fragmentAndNode.getKey();
                reader = (inputFormat == Format.Type.LINE)? new LineFormat(fragmentName) : new KVFormat(fragmentName);
                writer = new KVFormat(fragmentName + "-map");
                String workerUrl = "//" + fragmentAndNode.getValue().getHostname() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
                MapWorker worker = (MapWorker) Naming.lookup(workerUrl);
                worker.runMap(mr, reader, writer, new Job());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onMapFinished() throws RemoteException {
        remainingFragments--;
        System.out.println("NOTIFICATION REÇUE : un map terminé " + remainingFragments + " restant(s)");
        if(remainingFragments == 0){
            System.out.println("Tous les maps sont terminés!");
            String mergeFilename = mergeResFragments();
            reader = new KVFormat(mergeFilename);
            reader.open(Format.OpenMode.R);
            writer = new KVFormat(inputFname + "-reduce");
            writer.open(Format.OpenMode.W);
            mapReduce.reduce(reader, writer);
            System.out.println("Reduce Terminé!");
        }

    }

    /**
     * Lis via hdfs les fichiers résultants de l'exécution de chaque map lancé par le job
     * et crée un nouveau fichier en concaténant le tout.
     * @return le nom du fichier d'agrégation créé
     */
    private String mergeResFragments(){
        String outputFilename = inputFname + "-map";
        try {
            OutputStream out = new FileOutputStream(outputFilename);
            byte[] buf = new byte[1024];
            // Lecture de chaque fichier résultat avec HDFS et ajout en fin de {out}
            for (int i = 1; i <= numberFragments; i++) {
                String fragmentResName = inputFname + ".frag." + i + "-map";
                client.HdfsRead(fragmentResName, fragmentResName);
                InputStream in = new FileInputStream(fragmentResName);
                int b = 0;
                while ((b = in.read(buf)) >= 0)
                    out.write(buf, 0, b);
                in.close();
            }
            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputFilename;
    }
}
