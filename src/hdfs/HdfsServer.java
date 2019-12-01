package hdfs;

import config.Config;
import utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HdfsServer extends UnicastRemoteObject implements HdfsServerIt {

	private static final long serialVersionUID = 2400320909507772687L;
	public static long tailleFragment;
	public static ServerSocket server;
	public static Socket socket;
	public static InputStream input;
	public static OutputStream output;

	protected HdfsServer() throws RemoteException {
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

	public void execCmd(Commande cmd, String fichier, long taillefrag) throws IOException, InterruptedException {
		System.out.print("~~");
		switch (cmd) {
		case CMD_READ:
			envoyerFichier(fichier);
			break;
		case CMD_WRITE:
			tailleFragment = taillefrag;
			recevoirFichier(fichier);
			break;
		case CMD_DELETE:
			deleteFichier(fichier);
			break;
		case CMD_FIN:
			close();
			break;
		}
	}

	private void envoyerFichier(String nameFile) {
		try {
			File file = new File(nameFile);
			byte[] bytes = Files.readAllBytes(file.toPath());
			output.write(bytes, 0, bytes.length);
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
		input = socket.getInputStream();
		output = socket.getOutputStream();
		byte[] bytes = new byte[Math.toIntExact(tailleFragment)];
		int len = input.read(bytes);
		FileOutputStream stream = new FileOutputStream(fichier);
		// recuperation du fragment
		stream.write(bytes, 0, len);
		File file = File.createTempFile(fichier, "./");
		Fragmenter.toFichier(file, stream.toString());
		System.out.println("Reception du fragment " + fichier);
		stream.close();

	}

	public static void main(String[] args) {
		Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
		try {
			HdfsServer obj = new HdfsServer();
			String hdfsServerUrl = "//" + InetAddress.getLocalHost().getHostName() + ":" + Config.RMIREGISTRY_PORT + "/HdfsServer";
			Naming.rebind(hdfsServerUrl, obj);
			server = new ServerSocket(Config.HDFS_SERVER_PORT);
			socket = server.accept();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}