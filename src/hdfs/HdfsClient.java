/* une PROPOSITION de squelette, incomplète et adaptable... */

package hdfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import formats.Format;
import formats.KV;
import formats.KVFormat;
import formats.LineFormat;

public class HdfsClient extends UnicastRemoteObject implements HdfsClientIt {

	private static final long serialVersionUID = 135101440387648856L;
	// probleme encore avec les noms des fragments
	// les indices des fichiers
	public static HashMap<String, ArrayList<Pair<Integer, String>>> filesIndex = new HashMap<String, ArrayList<Pair<Integer, String>>>();
	//
	private static List<Socket> sockets = new ArrayList<Socket>();
	private static List<InputStream> inputStreams = new ArrayList<InputStream>();
	private static List<OutputStream> outputStreams = new ArrayList<OutputStream>();
	private static List<HdfsServerIt> servers = new ArrayList<HdfsServerIt>();
	private static int nbServeurs = 2;
	private static int tailleMax = 256;
	private static int portRMI = 2222;
	private static int portSocket = 3333;
	private static File[] fragments;

	protected HdfsClient() throws RemoteException {
		super();
	}

	public HashMap<String, ArrayList<Pair<Integer, String>>> filesIndex() {
		return this.filesIndex();
	}

	private static void fragmenter(String fichier, int nbFrags, String dest, Format.Type type) {
		try {
			fragments = Fragmenter.fragmenterFichier(fichier, nbFrags, dest, type);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void usage() {
		System.out.println("Usage: java HdfsClient read <file>");
		System.out.println("Usage: java HdfsClient write <line|kv> <file>");
		System.out.println("Usage: java HdfsClient delete <file>");
	}

	public static void lancerStubsETsockets() {
		try {
			Registry registry = LocateRegistry.createRegistry(portRMI-1);
			HdfsClient client = new HdfsClient();
			String URL = "//localhost:" + (portRMI-1) + "/HdfsClient";
			Naming.rebind(URL, client);
			for (int i = 0; i < nbServeurs; i++) {
				// recuperer les stubs
				servers.add((HdfsServerIt) Naming.lookup("//localhost" + ":" + (portRMI + i) + "/HdfsServer"));
				System.out.println("Connexion à //localhost" + ":" + (portRMI + i) + "/HdfsServer");
				// demande de connexions
				sockets.add(new Socket("localhost", portSocket + i));
				System.out.println("connexion acceptée sur le port = " + (portSocket + i));
				// lancer les <input|output>Stream
				inputStreams.add(sockets.get(i).getInputStream());
				outputStreams.add(sockets.get(i).getOutputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void HdfsDelete(String hdfsFname) {
		for (int i = 0; i < fragments.length; i++) {
			int numServer = Math.floorMod(i, nbServeurs);
			(new ExecCommande(servers.get(numServer), hdfsFname + ".frag." + i + "." + numServer, Commande.CMD_DELETE,
					0)).start();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		filesIndex.remove(hdfsFname);
	}

	public static void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor) {
		try {
			fragments = Fragmenter.fragmenterFichier(localFSSourceFname, tailleMax, "fragments", fmt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<Pair<Integer, String>> listeDesFrag = new ArrayList<>();
		for (int i = 0; i < fragments.length; i++) {
			File frag = fragments[i];
			int numServer = Math.floorMod(i, nbServeurs);
			try {
				(new ExecCommande(servers.get(numServer), localFSSourceFname + ".frag." + i + "." + numServer,
						Commande.CMD_WRITE, frag.length())).start();
				byte[] bytes = Files.readAllBytes(frag.toPath());
				outputStreams.get(numServer).write(bytes, 0, bytes.length);
				Pair<Integer, String> indice = new Pair<Integer, String>(i, String.valueOf(numServer));
				listeDesFrag.add(indice);
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		filesIndex.put(localFSSourceFname, listeDesFrag);
		System.out.println(filesIndex.toString());
	}

	public static void HdfsRead(String hdfsFname, String localFSDestFname) {

		try {
			File file = File.createTempFile(localFSDestFname, "./");
			FileOutputStream stream = new FileOutputStream(localFSDestFname);
			byte[] bytes = new byte[1000];
			int len;
			for (int i = 0; i < fragments.length; i++) {
				int numServer = Math.floorMod(i, nbServeurs);
				(new ExecCommande(servers.get(numServer), hdfsFname + ".frag." + i + "." + numServer, Commande.CMD_READ,
						0)).start();
				InputStream input = inputStreams.get(numServer);
				len = input.read(bytes);
				// recuperation des fragments
				stream.write(bytes, 0, len);
				System.out.println("--Reception du fragments " + i + " ...OK");
				Thread.sleep(500);
			}
			Fragmenter.toFichier(file, stream.toString());
			System.out.println("--Concatenation des fragments ... " + "\n--Fichier " + localFSDestFname + " crée");
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
			lancerStubsETsockets();
			HdfsWrite(Format.Type.LINE, "file.line", 1);
			Thread.sleep(1000);
			HdfsRead("file.line", "file.res.line");
			Thread.sleep(1000);
			HdfsDelete("file.line");

//			if (args.length < 2) {
//				usage();
//				return;
//			}
//
//			switch (args[0]) {
//			case "read":
//				HdfsRead(args[1], "file_rec.line");
//				break;
//			case "delete":
//				HdfsDelete(args[1]);
//				break;
//			case "write":
//				Format.Type fmt;
//
//				if (args.length < 3) {
//					usage();
//					return;
//				}
//				if (args[1].equals("line"))
//					fmt = Format.Type.LINE;
//				else if (args[1].equals("kv"))
//					fmt = Format.Type.KV;
//				else {
//					usage();
//					return;
//				}
//				HdfsWrite(fmt, args[2], 1);
//			}
			for (int i = 0; i < nbServeurs; i++) {
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
