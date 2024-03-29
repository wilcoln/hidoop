package hdfs;

import config.Config;
import utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DataNode extends UnicastRemoteObject implements DataNodeIt {

	private static final long serialVersionUID = 2400320909507772687L;
	public static int tailleFragment;
	public static ServerSocket server;
	public static Socket socket;
	public static InputStream input;
	public static OutputStream output;

	protected DataNode() throws RemoteException {
		super();
	}

	private void close() {
		try {
			input.close();
			output.close();
			socket.close();
			server.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void envoyerFichier(String nameFile) {
		try {
			File file = new File(Config.STORAGE_PATH +"/"+nameFile);
			int fileSize = (int) file.length();
			String fileName = nameFile;
			String FileToSend = Utils.multiString(",", 64 - (fileName.length())) + fileName;
			String cmd = Utils.multiString(",", 16 - ("CMD_READ".length())) + "CMD_READ";
			byte[] bytes = (Utils.multiString("0", (int) (16 - (fileSize + "").length())) + fileSize + FileToSend + cmd)
					.getBytes();
			output.write(bytes, 0, bytes.length);

			// Envoie du fichier
			BufferedReader bufReader = new BufferedReader(new FileReader(file));
			String ligne;
			while ((ligne = bufReader.readLine()) != null) {
				bytes = (ligne + "\n").getBytes();
				output.write(bytes, 0, bytes.length);
			}
			bufReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deleteFichier(String fichier) {
		File file = new File(Config.STORAGE_PATH + "/" +fichier);
		try {
			if(file.exists()){
				Files.deleteIfExists(file.toPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void recevoirFichier(String fichier) throws FileNotFoundException, IOException {
		String fname = Config.STORAGE_PATH +"/" +fichier;
		int len;
		FileOutputStream stream = new FileOutputStream(fname);
		int tailleRestante = tailleFragment;
		byte[] bytes = new byte[Math.min(512, tailleFragment)];
		while (tailleRestante != 0) {
			len = input.read(bytes);
			// recuperation du fragment
			stream.write(bytes, 0, len);
			tailleRestante = tailleRestante - len;
			bytes = new byte[Math.min(512, tailleRestante)];
		}
		File file = File.createTempFile(fname, "");
		Utils.toFichier(file, stream.toString());
		stream.close();
	}

	public static void main(String[] args) {
		Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
		try {
			DataNode obj = new DataNode();
			String ipAddress = InetAddress.getLocalHost().getHostAddress();
			String dataNodeUrl = "//" + ipAddress + ":" + Config.RMIREGISTRY_PORT + "/DataNode";
			System.setProperty("java.rmi.server.hostname", ipAddress);
			Naming.rebind(dataNodeUrl, obj);
			server = new ServerSocket(Config.DATANODE_PORT);
			while (true) {
			socket = server.accept();
			input = socket.getInputStream();
			output = socket.getOutputStream();
			// Analyser les commandes reçu 
			byte[] bytes = new byte[96];
			int len = input.read(bytes);
			while (len == 96) {
				String[] infos = Utils.splitStr(Utils.bytes2String(bytes), ",");
				tailleFragment = Integer.parseInt(infos[0]);
				String nomfichier = infos[1];
				String cmd = infos[2];
				// Execution des commandes
				switch (cmd) {
				case "CMD_READ":
					obj.envoyerFichier(nomfichier);
					break;
				case "CMD_WRITE":
					obj.recevoirFichier(nomfichier);
					break;
				case "CMD_DELETE":
					obj.deleteFichier(nomfichier);
					break;
				case "CMD_FIN":
					obj.close();
					break;
				}
				bytes = new byte[96];
				len = input.read(bytes);
			}
			input.close();
			output.close();
			socket.close();
		}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}