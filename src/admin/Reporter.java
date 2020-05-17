package admin;

import config.Config;
import config.Property;
import utils.ClusterNode;

import java.rmi.Naming;

public class Reporter {
    private static void reportMaster() throws Exception {
        // Où se trouve le namenode
        // Informations sur le stockage : nombre de fichiers dans le catalogue, taille total des fichiers stockés
        System.out.println("===============================================\n\t\tMASTER\n===============================================");
        // Etat du Démon NameNode
        String nameNodeReport = "\tLocation : " + Config.MASTER.toString() + "\n";
        String namenodeUrl = "//" + Config.MASTER.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/NameNode";;
        try {
            Naming.lookup(namenodeUrl);
            nameNodeReport += "\tNameNode UP\n";
        } catch (Exception e) {
            nameNodeReport += "\tNameNode DOWN\n";
        }
        System.out.println(nameNodeReport);
    }

    private static void reportWorker(ClusterNode worker) {
        String datanodeUrl, mapWorkerUrl, workerReport;
        datanodeUrl = "//" + worker.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/DataNode";
        mapWorkerUrl = "//" + worker.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
        workerReport = "\tLocation : " + worker.toString() + "\n";
        // Etat du Démon DataNode
        try {
            Naming.lookup(datanodeUrl);
            workerReport += "\tDataNode UP\n";
        } catch (Exception e) {
            workerReport += "\tDataNode DOWN\n";
        }

        // Etat du Démon MapWorker
        try {
            Naming.lookup(mapWorkerUrl);
            workerReport += "\tMapWorker UP\n";
            // TODO : workerReport+= usage disk sur le datanode
        } catch (Exception e) {
            workerReport += "\tMapWorker DOWN\n";
        }

        // Utilisation du disk
        try {
            String diskUsage = "Undefined";// TODO get disk usage on datanode
            workerReport += "\tDisk Usage : " + diskUsage + "\n";
            // TODO : workerReport+= usage disk sur le datanode
        } catch (Exception ignored) {
        }

        System.out.println(workerReport);
    }

    private static void reportWorkers() throws Exception {

        System.out.println("===============================================\n\t\tWORKERS\n===============================================");
        for (int i = 0; i < Config.WORKERS.size(); i++) {
            System.out.println("> Worker N° " + (i + 1));
            reportWorker(Config.WORKERS.get(i));
            System.out.println("--");
        }
    }

    private static void reportConfigs() {
        System.out.println("===============================================\n\t\tCONFIGURATIONS\n===============================================");
        System.out.println("> Core settings\n");
        System.out.println("\t" + Property.RMIREGISTRY_PORT + " : " + Config.RMIREGISTRY_PORT);
        System.out.println("\t" + Property.MASTER + " : " + Config.MASTER);
        System.out.println("\t" + Property.WORKERS + " (" + Config.WORKERS.size() + ") : ");
        for (ClusterNode worker : Config.WORKERS) {
            System.out.println("\t\t" + worker);
        }
        System.out.println();
        System.out.println("> Hdfs settings\n");
        System.out.println("\t" + Property.REP_FACTOR + " : " + Config.REP_FACTOR);
        System.out.println("\t" + Property.BLOC_SIZE + " : " + Config.MAX_BLOC_SIZE);
        System.out.println("\t" + Property.TMP_PATH + " : " + Config.TMP_PATH);
        System.out.println("\t" + Property.STORAGE_PATH + " : " + Config.STORAGE_PATH);
    } //


    private static void reportAll() throws Exception {
        reportConfigs();
        reportMaster();
        reportWorkers();
    }

    public static void main(String[] args) throws Exception {
        if(args.length > 0){
            switch (args[0]) {
                case "configs":
                    Reporter.reportConfigs();
                    break;
                case "master":
                    Reporter.reportMaster();
                    break;
                case "worker":
                    ClusterNode worker = Config.getWorkerByHostname(args[1]);
                    if (worker != null)
                        Reporter.reportWorker(worker);
                    else
                        System.out.println("Worker not Found!, Check your configurations");
                    break;
                case "workers":
                    Reporter.reportWorkers();
                    break;
                case "all":
                    Reporter.reportAll();
                    break;
            }
        }else{
            Reporter.reportAll();
        }
    }
}
