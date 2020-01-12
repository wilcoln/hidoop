package utils;

import config.Config;
import hdfs.NameNodeIt;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	/**
	 * Delete local file if exists
	 *
	 * @params filename
	 */
	public static void deleteFromLocal(String filename) {
		File file = new File(filename);
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createRegistryIfNotRunning(int rmiregistryPort) {
		// Si Registre tourne déjà sur le port, une exception est lancée et on l'attrape
		try {
			LocateRegistry.createRegistry(rmiregistryPort);
		} catch (RemoteException e) {

		}
	}

	public static NameNodeIt fetchNameNode() {
		NameNodeIt namenode = null;
		try {
			namenode = (NameNodeIt) Naming
					.lookup("//" + Config.MASTER.getIpAddress() + ":" + Config.RMIREGISTRY_PORT + "/NameNode");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return namenode;
	}

	public static String lsHdfsFile(String hdfsFname, ArrayList<Pair<Integer, ClusterNode>> index){
		StringBuilder result = new StringBuilder(hdfsFname + " => [\n");
		for (Pair<Integer, ClusterNode> fragAndNode : index) {
			result.append("\t(").append(fragAndNode.getKey()).append(", ").append(fragAndNode.getValue().getHostname()).append(");\n");
		}
		result.append("]\n");
		return result.toString();
	}

	public static int bytes2int(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < bytes.length; i++) {
			try {
				result += Math.pow(10, bytes.length - i - 1) * Integer.parseInt(String.valueOf((char) bytes[i]));
			} catch (Exception e) {
				System.out.println("\n" + (char) bytes[i] + ": fait gafff");
			}
		}
		return result;
	}

	public static String multiString(String str, int nb) {
		String s = "";
		for (int j = 0; j < nb; j++) {
			s += str;
		}
		return s;
	}

	public static String bytes2String(byte[] bytes) {
		String str = "";
		for (int i = 0; i < length(bytes); i++) {
			str += String.valueOf((char) bytes[i]);
		}
		return str;
	}

	public static int length(byte[] bytes) {
		// TODO Auto-generated method stub
		int i = 0;
		for (byte b : bytes) {
			i++;
			if (b == 0) {
				break;
			}
		}
		return i;
	}

	public static String[] splitStr(String str, String spliter) {
		List<String> listToReturn = new ArrayList<String>();
		String[] list = str.split(spliter);
		for (int i = 0; i < list.length; i++) {
			if (!list[i].equals("")) {
				listToReturn.add(list[i]);
			}
		}
		String[] toReturn = new String[listToReturn.size()];
		int count = 0;
		for (String s : listToReturn) {
			toReturn[count] = s;
			count++;
		}
		return toReturn;
	}
}
