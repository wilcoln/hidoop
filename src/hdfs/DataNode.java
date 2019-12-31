package hdfs;

import config.Config;
import ordo.MapWorker;
import utils.Log;
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
			System.out.println("Fin de Tâche .... OK");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void envoyerFichier(String nameFile) {
		try {
			File file = new File(nameFile);
			int fileSize = (int) file.length();
			String fileName = nameFile;
			String FileToSend = Utils.multiString("/", 64 - (fileName.length())) + fileName;
			String cmd = Utils.multiString("/", 16 - ("CMD_READ".length())) + "CMD_READ";
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
			System.out.println("Envoi du fichier " + nameFile + " ...OK ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deleteFichier(String fichier) {
		File file = new File(fichier);
		try {
			Files.deleteIfExists(file.toPath());
			System.out.println("fichier: " + fichier + " supprimé");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void recevoirFichier(String fichier) throws FileNotFoundException, IOException {

		int len;
		FileOutputStream stream = new FileOutputStream(fichier);
		int tailleRestante = tailleFragment;
		byte[] bytes = new byte[Math.min(512, tailleFragment)];
		while (tailleRestante != 0) {
			len = input.read(bytes);
			// recuperation du fragment
			stream.write(bytes, 0, len);
			tailleRestante = tailleRestante - len;
			bytes = new byte[Math.min(512, tailleRestante)];
		}
		File file = File.createTempFile(fichier, "");
		Fragmenter.toFichier(file, stream.toString());
		stream.close();
		System.out.println("Reception du fragment " + fichier);
	}

	public static void main(String[] args) {
		Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
		try {
			DataNode obj = new DataNode();
			String hostname = InetAddress.getLocalHost().getHostName();
			String dataNodeUrl = "//" + hostname + ":" + Config.RMIREGISTRY_PORT + "/DataNode";
			System.setProperty("java.rmi.server.hostname", hostname);
			Naming.rebind(dataNodeUrl, obj);
			Log.s("DataNode", "Hdfs Server enregistré à " + dataNodeUrl);
			//
			server = new ServerSocket(Config.HDFS_SERVER_PORT);
			socket = server.accept();
			input = socket.getInputStream();
			output = socket.getOutputStream();
			// Analyser les commandes reçu 
			byte[] bytes = new byte[96];
			int len = input.read(bytes);
			while (len == 96) {
				String[] infos = Utils.splitStr(Utils.bytes2String(bytes), "/");
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

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}