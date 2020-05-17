package hdfs;

import utils.ClusterNode;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface NameNodeIt extends Remote {
    Map<Integer, List<ClusterNode>> get(String fname) throws RemoteException;
    void put(String fname, Map<Integer, List<ClusterNode>> frags) throws RemoteException;
    void remove(String fname) throws RemoteException;
    String getInfoFile(String hdfsFname) throws RemoteException;
    String getInfoFiles() throws RemoteException;
}
