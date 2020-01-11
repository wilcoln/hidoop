/* une PROPOSITION de squelette, incomplète et adaptable... */

package hdfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import config.Config;
import formats.Format;
import utils.*;

public class HdfsClient implements HdfsClientIt {

	private final long serialVersionUID = 135101440387648856L;
	// les indices des fichiers
	private List<Socket> sockets = new ArrayList<Socket>();
	private List<InputStream> inputStreams = new ArrayList<InputStream>();
	private List<OutputStream> outputStreams = new ArrayList<OutputStream>();
	private List<DataNodeIt> servers = new ArrayList<DataNodeIt>();
	private int tailleMax = Config.MAX_BLOC_SIZE;
	private String[] fragments;
	private NameNodeIt nameNode;

	public HdfsClient(){
		super();
		this.nameNode = Utils.fetchNameNode();
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
			for (ClusterNode worker : Config.WORKERS) {
				// demande de connexions
				sockets.add(new Socket(worker.getHostname(), Config.DATANODE_PORT));
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
				int numServer = Math.floorMod(i, Config.WORKERS.size());
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
		nameNode.remove(hdfsFname);
	}

	public synchronized void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor)
			throws RemoteException {
		try {
			fragments = Fragmenter.fragmenterFichier(localFSSourceFname, tailleMax, Config.FRAGMENTS_PATH, fmt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<Pair<Integer, ClusterNode>> listeDesFrag = new ArrayList<>();
		for (int i = 0; i < fragments.length; i++) {
			File frag = new File(fragments[i]);
			int numServer = Math.floorMod(i, Config.WORKERS.size());
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
				Pair<Integer, ClusterNode> indice = new Pair<>(i, Config.WORKERS.get(numServer));
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
		nameNode.put(localFSSourceFname, listeDesFrag);
		Log.d("HdfsClient", nameNode.filesIndex2String());
	}

	public void HdfsRead(String hdfsFname, String localFSDestFname) throws RemoteException {
		try {

			File file = File.createTempFile(localFSDestFname, "");
			FileOutputStream stream = new FileOutputStream(localFSDestFname);
			int len;
			int tailleFichier;
			for (int i = 0; i < fragments.length; i++) {
				int numServer = Math.floorMod(i, Config.WORKERS.size());
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

	public NameNodeIt getNameNode(){
		return this.nameNode;
	}

	public void closeServers() throws IOException {
		for (int i = 0; i < Config.WORKERS.size(); i++) {
			inputStreams.get(i).close();
			outputStreams.get(i).close();
			sockets.get(i).close();
		}
	}

	public static void main(String[] args) throws Exception {
		HdfsClient hc = new HdfsClient();
		switch (args[0]){
			case "write":
				hc.HdfsWrite(Format.Type.LINE, args[1], 1);
				break;
			case "read":
				hc.HdfsRead(args[1], args[2]);
				break;
			case "delete":
				hc.HdfsDelete(args[1]);
				break;
		}
		hc.closeServers();
	}

}
