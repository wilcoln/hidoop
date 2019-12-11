/* une PROPOSITION de squelette, incomplète et adaptable... */

package hdfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import config.Config;
import formats.Format;
import utils.Node;
import utils.Pair;
import utils.Utils;

public class HdfsClient extends UnicastRemoteObject implements HdfsClientIt {

	private final long serialVersionUID = 135101440387648856L;
	// les indices des fichiers
	private HashMap<String, ArrayList<Pair<Integer, Node>>> filesIndex = new HashMap<>();
	private List<Socket> sockets = new ArrayList<Socket>();
	private List<InputStream> inputStreams = new ArrayList<InputStream>();
	private List<OutputStream> outputStreams = new ArrayList<OutputStream>();
	private List<HdfsServerIt> servers = new ArrayList<HdfsServerIt>();
	private int tailleMax = 1000000;
	private File[] fragments;

	protected HdfsClient() throws RemoteException {
		super();
	}

	public HashMap<String, ArrayList<Pair<Integer, Node>>> getFilesIndex() throws RemoteException {
		return this.filesIndex;
	}

	private void fragmenter(String fichier, int nbFrags, String dest, Format.Type type) {
		try {
			fragments = Fragmenter.fragmenterFichier(fichier, nbFrags, dest, type);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void usage() {
		System.out.println("Usage: java HdfsClient read <file>");
		System.out.println("Usage: java HdfsClient write <line|kv> <file>");
		System.out.println("Usage: java HdfsClient delete <file>");
	}

	public void lancerStubsETsockets() {
		try {
			Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
			String hostname = InetAddress.getLocalHost().getHostName();
			String hdfsClientUrl = "//" + hostname + ":" + Config.RMIREGISTRY_PORT + "/HdfsClient";
			System.setProperty("java.rmi.server.hostname", hostname);
			Naming.rebind(hdfsClientUrl, this);
			System.out.println("Hdfs Client bound in registry at " + hdfsClientUrl);
			for (Node worker : Config.workers) {
				// recuperer les stubs
				servers.add((HdfsServerIt) Naming
						.lookup("//" + worker.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/HdfsServer"));
				System.out.println(
						"Connexion à //" + worker.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/HdfsServer");
				// demande de connexions
				sockets.add(new Socket(worker.getHostname(), Config.HDFS_SERVER_PORT));
				System.out.println("connexion acceptée avec le datanode " + worker.getHostname() + " sur le port = "
						+ Config.RMIREGISTRY_PORT);
				// lancer les <input|output>Stream
				inputStreams.add(sockets.get(sockets.size() - 1).getInputStream());
				outputStreams.add(sockets.get(sockets.size() - 1).getOutputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void HdfsDelete(String hdfsFname) throws RemoteException {
		for (int i = 0; i < fragments.length; i++) {
			int numServer = Math.floorMod(i, Config.workers.size());
			(new ExecCommande(servers.get(numServer), hdfsFname + ".frag." + i, Commande.CMD_DELETE, 0)).start();
		}
		filesIndex.remove(hdfsFname);
	}

	public synchronized void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor) throws RemoteException {
		try {
			fragments = Fragmenter.fragmenterFichier(localFSSourceFname, tailleMax, Config.FRAGMENTS_PATH, fmt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<Pair<Integer, Node>> listeDesFrag = new ArrayList<>();
		for (int i = 0; i < fragments.length; i++) {
			File frag = fragments[i];
			int numServer = Math.floorMod(i, Config.workers.size());
			try {
				(new ExecCommande(servers.get(numServer), localFSSourceFname + ".frag." + i, Commande.CMD_WRITE,
						frag.length())).start();
				byte[] bytes = Files.readAllBytes(frag.toPath());
				outputStreams.get(numServer).write(bytes, 0, bytes.length);
				Pair<Integer, Node> indice = new Pair<>(i, Config.workers.get(numServer));
				listeDesFrag.add(indice);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		File directory = new File(Config.FRAGMENTS_PATH);
		for (File f : directory.listFiles()) {
			System.out.print("Suppression du fragment "+f);
			f.delete();
			System.out.println(" ...OK");
		}
		directory.delete();
		filesIndex.put(localFSSourceFname, listeDesFrag);
		System.out.println(filesIndex.toString());
	}

	public void HdfsRead(String hdfsFname, String localFSDestFname) throws RemoteException {

		try {
			File file = File.createTempFile(localFSDestFname, "");
			FileOutputStream stream = new FileOutputStream(localFSDestFname);
			byte[] bytes = new byte[1000];
			int len;
			for (int i = 0; i < fragments.length; i++) {
				int numServer = Math.floorMod(i, Config.workers.size());
				(new ExecCommande(servers.get(numServer), hdfsFname + ".frag." + i, Commande.CMD_READ, 0)).start();
				InputStream input = inputStreams.get(numServer);
				len = input.read(bytes);
				// recuperation des fragments
				stream.write(bytes, 0, len);
				System.out.println("--Reception du fragment " + i + " ...OK");
			}
			Fragmenter.toFichier(file, stream.toString());
			if ((new File(localFSDestFname + "")).exists()) {
				System.out.println("--Concatenation des fragments ... " + "\n--Fichier " + localFSDestFname + " crée");
			} else {
				System.out.println("fichier fragment n'a pas été crée");
			}
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void closeServers() throws IOException {
		for (int i = 0; i < Config.workers.size(); i++) {
			(new ExecCommande(servers.get(i), "", Commande.CMD_FIN, 0)).start();
			inputStreams.get(i).close();
			outputStreams.get(i).close();
			sockets.get(i).close();
		}
		System.out.println("##############################################################");
		System.out.println("###################### SEE YOU NEXT TIME #####################");
		System.out.println("##############################################################");
	}

	public static void main(String[] args) {
		// java HdfsClient <read|write> <line|kv> <file>
		try {
			System.out.println("##############################################################");
			System.out.println("###################### Welcome to Hidoop #####################");
			System.out.println("##############################################################");
			HdfsClient hdfsClient = new HdfsClient();
			hdfsClient.lancerStubsETsockets();
			hdfsClient.HdfsWrite(Format.Type.LINE, "file.line", 1);

			/*
			 * if (args.length < 2) { usage(); return; }
			 * 
			 * switch (args[0]) { case "read": HdfsRead(args[1], "file_rec.line"); break;
			 * case "delete": HdfsDelete(args[1]); break; case "write": Format.Type fmt;
			 * 
			 * if (args.length < 3) { usage(); return; } if (args[1].equals("line")) fmt =
			 * Format.Type.LINE; else if (args[1].equals("kv")) fmt = Format.Type.KV; else {
			 * usage(); return; } HdfsWrite(fmt, args[2], 1); }
			 */

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
