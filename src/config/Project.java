package config;

public class Project{
    public static String PATH="/home/wilcoln/Workspace/School/Semestre 1/Systèmes Concurrents/hidoop";
    public static String OUTPUT_PATH="/home/wilcoln/Workspace/School/Semestre 1/Systèmes Concurrents/hidoop/output";
    public static int MAPRED_WORKERS_PORT = 5120;
    public static int MAPRED_MASTER_PORT = 9000;
    public static int RMIREGISTRY_PORT = 5021;
    public static String[][] WORKERS = {{"interface", "192.168.122.1"}, {"master", "192.168.122.16"}};
}