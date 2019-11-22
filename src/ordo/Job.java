package ordo;

import config.Project;
import formats.*;
import hdfs.HdfsClient;
import map.MapReduce;
import util.Pair;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import static formats.Format.Type.LINE;

public class Job extends UnicastRemoteObject implements JobInterface, Callback {
    private static int remainingFragments = 0;
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

            //TODO: A supprimer
            HashMap<String, ArrayList<Pair<Integer, String>>> fileIndex = new HashMap<>();
            String nomfichier = "data/filesample.txt";
            Pair<Integer, String> fragement1WithHost = new Pair<>(1, "interface");
            Pair<Integer, String> fragment2WithHost = new Pair<>(2, "master");
            ArrayList<Pair<Integer, String>> infosFragmentsFichier = new ArrayList<>();
            infosFragmentsFichier.add(fragement1WithHost);
            infosFragmentsFichier.add(fragment2WithHost);
            fileIndex.put(nomfichier, infosFragmentsFichier);
            //^end
            System.out.println(fileIndex);

            // Lancement des maps sur les fragments
            remainingFragments = fileIndex.get(inputFname).size();
            for(Pair<Integer, String> fragmentWithHost: fileIndex.get(inputFname)) {
                String fragmentName = inputFname + ".frag." + fragmentWithHost.getKey();
                this.reader = (inputFormat == Format.Type.LINE)? new LineFormat(fragmentName) : new KVFormat(fragmentName);
                this.writer = new KVFormat(fragmentName + "-res");
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
        System.out.println("NOTIFICATION REÇU : un map terminé " + remainingFragments + " restant(s)");
        if(remainingFragments == 0){
            System.out.println("Tous les maps sont terminés!");
        }

    }
}
