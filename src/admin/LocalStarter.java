package admin;

import config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class LocalStarter {
    public static void main(String[] args) throws IOException {


        if(args.length == 1){
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

            String hostname = InetAddress.getLocalHost().getHostName();
            if(Config.MASTER.getHostname().equals(hostname) || Config.MASTER.getHostname().equals("localhost")){
                if(!jpsOutput.contains("NameNode")){
                    System.out.println("Starting NameNode...");
                    Runtime.getRuntime().exec(new String[]{Config.HIDOOP_HOME + "/bin/start.sh", "master"});
                }else{
                    System.out.println("A Master is already running on host, Stop it first.");
                }
            }
            if(Config.getWorkerByHostname(hostname) != null || Config.MASTER.getHostname().equals("localhost")){
                if(!jpsOutput.contains("DataNode")){
                    System.out.println("Starting DataNode and MapWorker...");
                    Runtime.getRuntime().exec(new String[]{Config.HIDOOP_HOME + "/bin/start.sh", "worker"});
                }else{
                    System.out.println("A Worker is already running on host, Stop it first.");
                }
            }

        }

        
    }
}
