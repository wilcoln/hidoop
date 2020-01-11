package config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.ClusterNode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

public class Config {
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

    static {
        try {
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void load() throws Exception{
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbFactory.newDocumentBuilder();
        Document doc;
        File xmlFile;
        NodeList nList;

        /**
         * Chargement de core-site.xml
         */

        xmlFile = new File("../config/core-site.xml");
        doc = db.parse(xmlFile);
        doc.getDocumentElement().normalize();

        nList = doc.getElementsByTagName("property");

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                Node value = eElement.getElementsByTagName("value").item(0);
                Node desc = eElement.getElementsByTagName("description").item(0);
                String hostname, ipAddress;
                switch (name) {
                    case Property.RMIREGISTRY_PORT:
                        RMIREGISTRY_PORT = Integer.parseInt(value.getTextContent());
                        break;

                    case Property.REP_FACTOR:
                        REP_FACTOR = Integer.parseInt(value.getTextContent());
                        break;

                    case Property.MASTER:
                        eElement = (Element) value;
                        hostname = eElement.getElementsByTagName("hostname").item(0).getTextContent();
                        ipAddress = eElement.getElementsByTagName("ip-address").item(0).getTextContent();
                        MASTER = new ClusterNode(hostname, ipAddress);
                        break;

                    case Property.WORKERS:
                        eElement = (Element) value;
                        int nbworkers = eElement.getElementsByTagName("hostname").getLength();
                        for(int i = 0; i < nbworkers; i++){
                            hostname = eElement.getElementsByTagName("hostname").item(i).getTextContent();
                            ipAddress = eElement.getElementsByTagName("ip-address").item(i).getTextContent();
                            WORKERS = new ArrayList<>();
                            WORKERS.add( new ClusterNode(hostname, ipAddress));
                        }
                        break;
                }

            }
        }


        /**
         * Chargement de hdfs-site.xml
         */

        xmlFile = new File("../config/hdfs-site.xml");
        doc = db.parse(xmlFile);
        doc.getDocumentElement().normalize();

        nList = doc.getElementsByTagName("property");

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                Node value = eElement.getElementsByTagName("value").item(0);
                Node desc = eElement.getElementsByTagName("description").item(0);
                switch (name) {
                    case Property.DATANODE_PORT:
                        DATANODE_PORT = Integer.parseInt(value.getTextContent());
                        break;

                    case Property.REP_FACTOR:
                        REP_FACTOR = Integer.parseInt(value.getTextContent());
                        break;

                    case Property.BLOC_SIZE:
                        MAX_BLOC_SIZE = Integer.parseInt(value.getTextContent());
                        break;

                    case Property.FRAGMENTS_PATH:
                        FRAGMENTS_PATH = value.getTextContent();
                        break;

                    case Property.NAMENODE_DATA_PATH:
                        NAMENODE_DATA_PATH = value.getTextContent();
                        break;

                    case Property.DATANODE_DATA_PATH:
                        DATANODE_DATA_PATH = value.getTextContent();
                        break;

                }

            }
        }


        /**
         * Chargement de mapred-site.xml
         */

        xmlFile = new File("../config/mapred-site.xml");
        doc = db.parse(xmlFile);
        doc.getDocumentElement().normalize();

        nList = doc.getElementsByTagName("property");

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                Node value = eElement.getElementsByTagName("value").item(0);
                Node desc = eElement.getElementsByTagName("description").item(0);
                switch (name) {
                    case Property.OUTPUT_PATH:
                        OUTPUT_PATH = value.getTextContent();
                        break;

                    case Property.INPUT_PATH:
                        INPUT_PATH = value.getTextContent();
                        break;
                }

            }
        }

    }
}
