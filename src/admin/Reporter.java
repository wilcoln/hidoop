package admin;

import config.Config;
import ordo.MapWorkerIt;
import utils.ClusterNode;

import java.rmi.Naming;
import java.util.ArrayList;

public class Admin {
    public void reportLivingDataNodes(){
        String datanodeUrl;
        ArrayList<ClusterNode> livingDataNodes = new ArrayList<>();
        for (Config.DATANODES:
             ) {

        }
         = "//" + fragAndNode.getValue().getHostname() + ":" + Config.RMIREGISTRY_PORT + "/MapWorker";
        MapWorkerIt worker = (MapWorkerIt) Naming.lookup(datanodeUrl);
        System.out.println("Living Nodes\n===========");
    }
    public void reportConfigs(){} //
    public void reportDiskUsage(){} //
    public void reportAll(){
        reportConfigs();
        reportNameNodeDiskUsage();
        reportLivingDataNodes();
        reportDataNodesDiskUsage();
    } // affiche tous les rapports
}
