package utils;

import config.Config;
import formats.Format;
import hdfs.HdfsClientIt;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Utils {
    /**
     * Delete local file if exists
     * @params filename
     */
    public static void deleteFromLocal(String filename){
        File file = new File(filename);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createRegistryIfNotRunning(int rmiregistryPort) {
        // Si Registre tourne déjà sur le port, une exception est lancée et on l'attrape
        try {
            LocateRegistry.createRegistry(rmiregistryPort);
        }catch (Exception e){

        }
    }
    public static HdfsClientIt fetchHdfsClient(){
        HdfsClientIt hdfsClient = null;
        try {
            System.out.print("Connecting to //" + Config.master.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/HdfsClient"  + "... ");
            hdfsClient = (HdfsClientIt) Naming.lookup("//" + Config.master.getIpAddress() + ":" + Config.RMIREGISTRY_PORT + "/HdfsClient");
            System.out.println("Successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hdfsClient;
    }

    public String stringConcat(Object ...args){
        String result = "";
        for(Object arg : args){
            result += arg.toString();
        }
        return result;
    }
}
