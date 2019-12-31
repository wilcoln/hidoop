package hdfs;

import utils.Node;
import utils.Pair;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface NameNodeIt extends Remote {
    ArrayList<Pair<Integer, Node>> get(String fname) throws RemoteException;
    void put(String fname, ArrayList<Pair<Integer, Node>> fragsAndNode) throws RemoteException;
    void remove(String fname) throws RemoteException;
    String filesIndex2String() throws RemoteException;
}
