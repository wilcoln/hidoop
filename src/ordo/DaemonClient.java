package ordo;

import config.Project;

import java.rmi.*;

public class DaemonClient {
    public static void main(String args[]){
        try {
            Daemon obj = (Daemon) Naming.lookup("//192.168.122.1:" + Project.RMIREGISTRY_PORT + "/mapred");
            System.out.println(obj);
            System.out.println(obj.test());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
