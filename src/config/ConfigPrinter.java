package config;

import utils.ClusterNode;
import utils.Log;

import java.util.ArrayList;

public class ConfigPrinter {
    public static void main(String[] args) {
        // Print core-site.xml
        /*// From core-site.xml
        public static int RMIREGISTRY_PORT;
        public static ClusterNode MASTER;
        public static ArrayList<ClusterNode> WORKERS;


        // From hdfs-site.xml
        public static String FRAGMENTS_PATH;
        public static int DATANODE_PORT;
        public static int REP_FACTOR;
        public static int MAX_BLOC_SIZE;
        public static String NAMENODE_DATA_PATH;
        public static String DATANODE_DATA_PATH;

        // From mapred-site.xml
        public static String OUTPUT_PATH;
        public static String INPUT_PATH;*/
        System.out.println("===============================\n\tCONFIGURATIONS\n===============================");
        System.out.println("> Core settings\n");
        System.out.println("\t"+Property.RMIREGISTRY_PORT + " : " + Config.RMIREGISTRY_PORT);
        System.out.println("\t"+Property.MASTER + " : " + Config.MASTER);
        System.out.println("\t"+Property.WORKERS +  " (" + Config.WORKERS.size() + ") : ");
        for (ClusterNode worker: Config.WORKERS) {
            System.out.println("\t\t" + worker);
        }
        System.out.println();
        System.out.println("> Hdfs settings\n");
        System.out.println("\t"+Property.REP_FACTOR + " : " + Config.REP_FACTOR);
        System.out.println("\t"+Property.BLOC_SIZE + " : " + Config.MAX_BLOC_SIZE);
        System.out.println("\t"+Property.NAMENODE_DATA_PATH + " : " + Config.NAMENODE_DATA_PATH);
        System.out.println("\t"+Property.FRAGMENTS_PATH + " : " + Config.FRAGMENTS_PATH);
        System.out.println("\t"+Property.DATANODE_DATA_PATH + " : " + Config.DATANODE_DATA_PATH);
        System.out.println("\t"+Property.DATANODE_PORT + " : " + Config.DATANODE_PORT);

        System.out.println();
        System.out.println("> MapRed settings\n");
        System.out.println("\t"+Property.INPUT_PATH + " : " + Config.INPUT_PATH);
        System.out.println("\t"+Property.OUTPUT_PATH + " : " + Config.OUTPUT_PATH);

    }
}
