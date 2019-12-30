/* une PROPOSITION de squelette, incomplète et adaptable... */

package hdfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import utils.*;

public class HdfsClient extends UnicastRemoteObject implements HdfsClientIt {

	private final long serialVersionUID = 135101440387648856L;
	// les indices des fichiers
	private List<Socket> sockets = new ArrayList<Socket>();
	private List<InputStream> inputStreams = new ArrayList<InputStream>();
	private List<OutputStream> outputStreams = new ArrayList<OutputStream>();
	private List<HdfsServerIt> servers = new ArrayList<HdfsServerIt>();
	private int tailleMax = Config.TAILLE_BLOC_MAX;
	private String[] fragments;
	private NameNode nameNode;

	public HdfsClient() throws RemoteException {
		super();
		this.nameNode = new NameNode();
		lancerStubsETsockets();
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
			for (Node worker : Config.workers) {
				// demande de connexions
				sockets.add(new Socket(worker.getHostname(), Config.HDFS_SERVER_PORT));
				Log.s("HdfsClient", "Succes");
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
			try {
				int numServer = Math.floorMod(i, Config.workers.size());
				// Envoi des infos sur le fichier à supprimer
				// format: 0...0///nomFichier///..CMD_READ
				int tailleFrag = 0;
				String nomFrag = hdfsFname + ".frag." + i;
				String FileToSend = Utils.multiString("/", 64 - (nomFrag.length())) + nomFrag;
				String cmd = Utils.multiString("/", 16 - ("CMD_DELETE".length())) + "CMD_DELETE";
				byte[] bytes = (Utils.multiString("0", (int) (16 - (tailleFrag + "").length())) + tailleFrag
						+ FileToSend + cmd).getBytes();
				outputStreams.get(numServer).write(bytes, 0, bytes.length);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		nameNode.getFilesIndex().remove(hdfsFname);
	}

	public synchronized void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor)
			throws RemoteException {
		try {
			fragments = Fragmenter.fragmenterFichier(localFSSourceFname, tailleMax, Config.FRAGMENTS_PATH, fmt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<Pair<Integer, Node>> listeDesFrag = new ArrayList<>();
		for (int i = 0; i < fragments.length; i++) {
			File frag = new File(fragments[i]);
			int numServer = Math.floorMod(i, Config.workers.size());
			try {

				// envoie des informations sur le fragment
				// Sous ce format: 0...01234/.../nomFrag/.../CMD_WRITE

				int tailleFrag = (int) frag.length();
				String fileName = localFSSourceFname + ".frag." + i;
				String FileToSend = Utils.multiString("/", 64 - (fileName.length())) + fileName;
				String cmd = Utils.multiString("/", 16 - ("CMD_WRITE".length())) + "CMD_WRITE";
				byte[] bytes = (Utils.multiString("0", (int) (16 - (tailleFrag + "").length())) + tailleFrag
						+ FileToSend + cmd).getBytes();
				outputStreams.get(numServer).write(bytes, 0, bytes.length);

				// Envoie du fragment
				BufferedReader bufReader = new BufferedReader(new FileReader(frag));
				String ligne;
				while ((ligne = bufReader.readLine()) != null) {
					bytes = (ligne + "\n").getBytes();
					outputStreams.get(numServer).write(bytes, 0, bytes.length);
				}
				Pair<Integer, Node> indice = new Pair<>(i, Config.workers.get(numServer));
				listeDesFrag.add(indice);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		File directory = new File(Config.FRAGMENTS_PATH);
		for (File f : directory.listFiles()) {
			System.out.print("Suppression du fragment " + f);
			f.delete();
			System.out.println(" ...OK");
		}
		directory.delete();
		nameNode.getFilesIndex().put(localFSSourceFname, listeDesFrag);
		Log.d("HdfsClient", Utils.filesIndex2String(nameNode.getFilesIndex()));
	}

	public void HdfsRead(String hdfsFname, String localFSDestFname) throws RemoteException {

		try {

			File file = File.createTempFile(localFSDestFname, "");
			FileOutputStream stream = new FileOutputStream(localFSDestFname);
			int len;
			int tailleFichier;
			for (int i = 0; i < fragments.length; i++) {
				int numServer = Math.floorMod(i, Config.workers.size());
				InputStream input = inputStreams.get(numServer);

				// envoie des informations sur le fragment
				// format: 0...0///nomFichier///..CMD_READ
				int tailleFrag = 0;
				String nomFrag = hdfsFname + ".frag." + i;
				String FileToSend = Utils.multiString("/", 64 - (nomFrag.length())) + nomFrag;
				String cmd = Utils.multiString("/", 16 - ("CMD_READ".length())) + "CMD_READ";
				byte[] bytes = (Utils.multiString("0", (int) (16 - (tailleFrag + "").length())) + tailleFrag
						+ FileToSend + cmd).getBytes();
				outputStreams.get(numServer).write(bytes, 0, bytes.length);

				// Recevoir les infos sur le fichier
				bytes = new byte[96];
				len = input.read(bytes);
				String[] infos = Utils.splitStr(Utils.bytes2String(bytes), "/");

				// Recuperer le nom et la taille
				tailleFichier = Integer.parseInt(infos[0]);
				String nomfichier = infos[1];

				int tailleRestante = tailleFichier;
				bytes = new byte[Math.min(512, tailleRestante)];
				// Recevoir le fichier
				while (tailleRestante != 0) {
					len = input.read(bytes);
					// recuperation du fragment
					stream.write(bytes, 0, len);
					tailleRestante = tailleRestante - len;
					bytes = new byte[Math.min(512, tailleRestante)];
				}
				System.out.println("--Reception du fichier " + i + " ...OK");
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

	public NameNode getNameNode() throws RemoteException {
		return this.nameNode;
	}

	public void closeServers() throws IOException {
		for (int i = 0; i < Config.workers.size(); i++) {
			inputStreams.get(i).close();
			outputStreams.get(i).close();
			sockets.get(i).close();
		}
	}

}
