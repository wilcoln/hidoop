/* une PROPOSITION de squelette, incomplète et adaptable... */

package hdfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import config.Config;
import formats.Format;
import formats.KV;
import formats.KVFormat;
import formats.LineFormat;
import utils.Node;
import utils.Pair;
import utils.Utils;

public class HdfsClient extends UnicastRemoteObject implements HdfsClientIt {

	private final long serialVersionUID = 135101440387648856L;
	// probleme encore avec les noms des fragments
	// les indices des fichiers
	private HashMap<String, ArrayList<Pair<Integer, Node>>> filesIndex = new HashMap<>();
	//
	private static List<Socket> sockets = new ArrayList<Socket>();
	private static List<InputStream> inputStreams = new ArrayList<InputStream>();
	private static List<OutputStream> outputStreams = new ArrayList<OutputStream>();
	private static List<HdfsServerIt> servers = new ArrayList<HdfsServerIt>();
	private int tailleMax = 256;
	private File[] fragments;

	protected HdfsClient() throws RemoteException {
		super();
	}

	public HashMap<String, ArrayList<Pair<Integer, Node>>> getFilesIndex() throws RemoteException{
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


	public static void lancerStubsETsockets() {
		try {
			Utils.createRegistryIfNotRunning(Config.RMIREGISTRY_PORT);
			HdfsClient hdfsClient = new HdfsClient();
			String hdfsClientUrl = "//" + InetAddress.getLocalHost().getHostName() +  ":" + Config.RMIREGISTRY_PORT + "/HdfsClient";
			Naming.rebind(hdfsClientUrl, hdfsClient);
			for(Node worker : Config.workers) {
				// recuperer les stubs
				servers.add((HdfsServerIt) Naming.lookup("//" + worker.getHostname() + ":" + Config.RMIREGISTRY_PORT + "/HdfsServer"));
				System.out.println("Connexion à //" + worker.getHostname()  + ":" + Config.RMIREGISTRY_PORT + "/HdfsServer");
				// demande de connexions
				sockets.add(new Socket(worker.getHostname(), Config.HDFS_SERVER_PORT));
				System.out.println("connexion acceptée avec le datanode " + worker.getHostname() +  " sur le port = " + Config.RMIREGISTRY_PORT);
				// lancer les <input|output>Stream
				inputStreams.add(sockets.get(sockets.size() - 1).getInputStream());
				outputStreams.add(sockets.get(sockets.size() - 1).getOutputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void HdfsDelete(String hdfsFname) throws RemoteException{
		for (int i = 0; i < fragments.length; i++) {
			int numServer = Math.floorMod(i, Config.workers.size());
			(new ExecCommande(servers.get(numServer), hdfsFname + ".frag."+i, Commande.CMD_DELETE, 0)).start();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		filesIndex.remove(hdfsFname);
	}

	public void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor) throws RemoteException{
		try {
			fragments = Fragmenter.fragmenterFichier(localFSSourceFname, tailleMax, "fragments", fmt);
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
				Pair<Integer, Node> indice = new Pair<Integer, Node>(i, new Node(String.valueOf(numServer), ""));
				listeDesFrag.add(indice);
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		filesIndex.put(localFSSourceFname, listeDesFrag);
		System.out.println(filesIndex.toString());
	}

	public void HdfsRead(String hdfsFname, String localFSDestFname) throws RemoteException{

		try {
			
			File file = File.createTempFile(localFSDestFname, "./");
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
				System.out.println("--Reception du resultat " + i + " ...OK");
				Thread.sleep(500);
			}
			Fragmenter.toFichier(file, stream.toString());
			System.out.println("--Concatenation des resultats ... " + "\n--Fichier " + localFSDestFname + " crée");
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// java HdfsClient <read|write> <line|kv> <file>
		try {
			System.out.println("##############################################################");
			System.out.println("###################### Welcome to Hidoop #####################");
			System.out.println("##############################################################");
			HdfsClient client = new HdfsClient();
			lancerStubsETsockets();
/*			HdfsWrite(Format.Type.LINE, "file.line", 1);
			Thread.sleep(1000);
			HdfsRead("file.line", "file.res.line");
			Thread.sleep(1000);
			HdfsDelete("file.line");*/

/*			if (args.length < 2) {
				usage();
				return;
			}

			switch (args[0]) {
			case "read":
				HdfsRead(args[1], "file_rec.line");
				break;
			case "delete":
				HdfsDelete(args[1]);
				break;
			case "write":
				Format.Type fmt;

				if (args.length < 3) {
					usage();
					return;
				}
				if (args[1].equals("line"))
					fmt = Format.Type.LINE;
				else if (args[1].equals("kv"))
					fmt = Format.Type.KV;
				else {
					usage();
					return;
				}
				HdfsWrite(fmt, args[2], 1);
			}*/
			for (int i = 0; i < Config.workers.size(); i++) {
				(new ExecCommande(servers.get(i), "", Commande.CMD_FIN, 0)).start();
				inputStreams.get(i).close();
				outputStreams.get(i).close();
				sockets.get(i).close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("##############################################################");
		System.out.println("###################### SEE YOU NEXT TIME #####################");
		System.out.println("##############################################################");
		System.exit(0);

	}

}
