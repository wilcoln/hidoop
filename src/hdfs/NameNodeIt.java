package hdfs;

import utils.ClusterNode;
import utils.Pair;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface NameNodeIt extends Remote {
    ArrayList<Pair<Integer, ClusterNode>> get(String fname) throws RemoteException;
    void put(String fname, ArrayList<Pair<Integer, ClusterNode>> fragsAndNode) throws RemoteException;
    void remove(String fname) throws RemoteException;
    String getInfoFile(String hdfsFname) throws RemoteException;
    String getInfoFiles() throws RemoteException;
}
