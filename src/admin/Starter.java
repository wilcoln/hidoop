package admin;

import config.Config;
import utils.ClusterNode;

import java.io.*;
import java.net.InetAddress;

public class Starter {
    public static void main(String[] args) throws IOException {

        // Get jps output
        String jpsOutput = "";

        Process proc = Runtime.getRuntime().exec("jps");
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        // Read the output from the command
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            jpsOutput+=s+"\n";
        }

        // Get hostname
        String hostname = InetAddress.getLocalHost().getHostName();

        //Start appropriate daemons
        if(Config.MASTER.getHostname().equals(hostname) || Config.MASTER.getHostname().equals("localhost")){
            if(!jpsOutput.contains("NameNode")){
                System.out.println("Starting NameNode...");
                Runtime.getRuntime().exec(new String[]{Config.HIDOOP_HOME + "/bin/start.sh", "master"});
                // Write workers list file
                createWorkersListFile();

            }else{
                System.out.println("A Master is already running on host, Stop it first.");
            }
        }
        if (Config.getWorkerByHostname(hostname) != null || Config.MASTER.getHostname().equals("localhost")){
            if(!jpsOutput.contains("DataNode")){
                System.out.println("Starting DataNode and MapWorker...");
                Runtime.getRuntime().exec(new String[]{Config.HIDOOP_HOME + "/bin/start.sh", "worker"});
            }else{
                System.out.println("A Worker is already running on host, Stop it first.");
            }
        }

    }

    private static void createWorkersListFile() throws  IOException{
        PrintWriter printWriter = new PrintWriter(Config.HIDOOP_HOME+"/config/.workers");
        for (ClusterNode worker: Config.WORKERS) {
            printWriter.println(worker.getHostname());
        }
        printWriter.close();
    }
}
