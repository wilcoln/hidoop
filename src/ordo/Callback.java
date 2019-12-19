package ordo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Callback extends UnicastRemoteObject implements CallbackIt {
    private Job job;
    public Callback(Job job) throws RemoteException {
        this.job = job;
    }
    private static final long serialVersionUID = 1L;
    @Override
    public void onMapFinished() throws RemoteException {
        job.onMapFinished();
    }
}