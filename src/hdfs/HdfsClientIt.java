package hdfs;

import formats.Format;

import java.rmi.RemoteException;


public interface HdfsClientIt {

	void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor) throws RemoteException;
	void HdfsRead(String hdfsFname, String localFSDestFname) throws RemoteException;
	void HdfsDelete(String hdfsFname) throws RemoteException;
	public NameNodeIt getNameNode() throws RemoteException;

}
