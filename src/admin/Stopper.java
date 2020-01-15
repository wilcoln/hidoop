package admin;

import config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class Stopper {
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

        String hostname = InetAddress.getLocalHost().getHostName();
        if(Config.MASTER.getHostname().equals(hostname) || Config.MASTER.getHostname().equals("localhost")){
            if(jpsOutput.contains("NameNode")){
                System.out.println("Stopping NameNode...");
                Runtime.getRuntime().exec(new String[]{Config.HIDOOP_HOME + "/bin/stop.sh", "master"});
            }else{
                System.out.println("No Master is running on host.");
            }
        }
        if(Config.getWorkerByHostname(hostname) != null || Config.MASTER.getHostname().equals("localhost")){
            if(jpsOutput.contains("DataNode")){
                System.out.println("Stopping DataNode and MapWorker...");
                Runtime.getRuntime().exec(new String[]{Config.HIDOOP_HOME + "/bin/stop.sh", "worker"});
            }else{
                System.out.println("No Worker is running on host.");
            }
        }

    }
}
