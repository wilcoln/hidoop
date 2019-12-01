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
}
