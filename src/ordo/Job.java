package ordo;

import config.Project;
import formats.*;
import hdfs.HdfsClient;
import map.MapReduce;
import util.Pair;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import static formats.Format.Type.LINE;

public class Job extends UnicastRemoteObject implements JobInterface, Callback {
    private static int remainingFragments = 0;
    private static int numberFragments = 0;
    private Format.Type inputFormat;
    private static String inputFname;
    private static MapReduce mapReduce;
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

            //TODO: A supprimer
            HashMap<String, ArrayList<Pair<Integer, String>>> fileIndex = new HashMap<>();
            String nomfichier = "data/filesample.txt";
            Pair<Integer, String> fragment1WithHost = new Pair<>(1, "interface");
            Pair<Integer, String> fragment2WithHost = new Pair<>(2, "master");
            ArrayList<Pair<Integer, String>> infosFragmentsFichier = new ArrayList<>();
            infosFragmentsFichier.add(fragment1WithHost);
            infosFragmentsFichier.add(fragment2WithHost);
            fileIndex.put(nomfichier, infosFragmentsFichier);
            //^end

            // Lancement des maps sur les fragments
            numberFragments = fileIndex.get(inputFname).size();
            remainingFragments = numberFragments;
            for(Pair<Integer, String> fragmentWithHost: fileIndex.get(inputFname)) {
                String fragmentName = inputFname + ".frag." + fragmentWithHost.getKey();
                this.reader = (inputFormat == Format.Type.LINE)? new LineFormat(fragmentName) : new KVFormat(fragmentName);
                this.writer = new KVFormat(fragmentName + "-map-res");
                String workerUrl = "//" + fragmentWithHost.getValue()+ ":" + Project.RMIREGISTRY_PORT + "/" + fragmentWithHost.getValue();
                HidoopWorker worker = (HidoopWorker) Naming.lookup(workerUrl);
                worker.runMap(mr, this.reader, this.writer, new Job());

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
            String mergeFilename = mergeFragments();
            this.reader = new KVFormat(mergeFilename);
            this.reader.open(Format.OpenMode.R);
            this.writer = new KVFormat(inputFname + "-reduce-res");
            this.writer.open(Format.OpenMode.W);
            mapReduce.reduce(reader, writer);
        }

    }
    private String mergeFragments(){
        String filename = inputFname + "-map-res";
        try {
            OutputStream out = new FileOutputStream(filename);
            byte[] buf = new byte[1024];
            // Lecture de chaque fichier résultat avec HDFS
            for (int i = 1; i <= numberFragments; i++) {
                String fragmentResName = inputFname + ".frag." + i + "-map-res";
                HdfsClient.HdfsRead(fragmentResName, fragmentResName);
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
        return filename;
    }
}
