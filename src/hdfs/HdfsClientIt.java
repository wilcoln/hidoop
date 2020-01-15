package hdfs;

import formats.Format;

import java.rmi.RemoteException;


public interface HdfsClientIt {

	void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor) throws Exception;
	void HdfsRead(String hdfsFname, String localFSDestFname) throws Exception;
	void HdfsDelete(String hdfsFname) throws Exception;
	void HdfsList(String... hdfsFnames) throws Exception;
	public NameNodeIt getNameNode() throws Exception;

}
