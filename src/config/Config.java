package config;

import utils.Node;

import java.util.ArrayList;

public class Config {
    public static String PATH="/home/wilcoln/Workspace/School/Semestre 1/Systèmes Concurrents/hidoop";
    public static String OUTPUT_PATH="/home/wilcoln/Workspace/School/Semestre 1/Systèmes Concurrents/hidoop/output";
    public static String FRAGMENTS_PATH = "./fragments";
    public static int HDFS_SERVER_PORT = 3333;
    public static int RMIREGISTRY_PORT = 5021;
    public static Node master;
    public static ArrayList<Node> workers;
    static {
        master = new Node("lando.enseeiht.fr", "147.127.133.204");
        workers = new ArrayList<>();
        workers.add(Node("lando.enseeiht.fr", "147.127.133.204"));
        workers.add(Node("lando.enseeiht.fr", "147.127.133.204"));
        workers.add(Node("lando.enseeiht.fr", "147.127.133.204"));   
    }
}   
