package config;

import utils.ClusterNode;

import java.util.ArrayList;

public class Printer {
    public static void main(String[] args) {
        // Print core-site.xml
        // From core-site.xml
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
        public static String INPUT_PATH;
    }
}
