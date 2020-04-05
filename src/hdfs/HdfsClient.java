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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import config.Config;
import formats.Format;
import utils.*;

public class HdfsClient implements HdfsClientIt {

	private final NameNodeIt nameNode;
	// les indices des fichiers
	private List<Socket> sockets = new ArrayList<Socket>();
	private List<InputStream> inputStreams = new ArrayList<InputStream>();
	private List<OutputStream> outputStreams = new ArrayList<OutputStream>();
	private int tailleMax = Config.MAX_BLOC_SIZE;
	private int[] spaceOnNodes = new int[Config.WORKERS.size()];

	public HdfsClient() {
		lancerStubsETsockets();
		nameNode = Utils.fetchNameNode();
	}

	private static void usage() {
		System.out.println("Usage: java HdfsClient read <file>");
		System.out.println("Usage: java HdfsClient write <line|kv> <file>");
		System.out.println("Usage: java HdfsClient delete <file>");
	}

	public void lancerStubsETsockets() {
		try {
			for (ClusterNode worker : Config.WORKERS) {
				// demande de connexions
				sockets.add(new Socket(worker.getHostname(), Config.DATANODE_PORT));
				// lancer les <input|output>Stream
				inputStreams.add(sockets.get(sockets.size() - 1).getInputStream());
				outputStreams.add(sockets.get(sockets.size() - 1).getOutputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void HdfsDelete(String hdfsFname) throws Exception {

		for (Pair<Integer, ClusterNode> pair : nameNode.get(hdfsFname)) {

			// recupere l'indice du dataNode sur lequel se trouve le fragment
			int numServer = Config.getIndexByHostname(pair.getValue().getHostname());

			// Envoi des infos sur le fichier à supprimer
			// format: 0...0,,,nomFichier,,,..CMD_READ
			int tailleFrag = 0;
			String nomFrag = hdfsFname + ".frag." + pair.getKey();
			String FileToSend = Utils.multiString(",", 64 - (nomFrag.length())) + nomFrag;
			String cmd = Utils.multiString(",", 16 - ("CMD_DELETE".length())) + "CMD_DELETE";
			byte[] bytes = (Utils.multiString("0", (int) (16 - (tailleFrag + "").length())) + tailleFrag + FileToSend
					+ cmd).getBytes();

			outputStreams.get(numServer).write(bytes, 0, bytes.length);
		}
		nameNode.remove(hdfsFname);
	}

	@Override
	public NameNodeIt getNameNode() throws Exception {
		return nameNode;
	}

	public void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor) throws Exception {

		// Fragmenter le fichier
		String[] fragments = Fragmenter.fragmenterFichier(localFSSourceFname, tailleMax, Config.TMP_PATH, fmt);

		// on n'utilisera que le nom du fichier pas tout son path
		String fname = localFSSourceFname.split("/")[localFSSourceFname.split("/").length - 1];

		// Initialiser la liste des fragments
		ArrayList<Pair<Integer, ClusterNode>> listeDesFrag = new ArrayList<>();

		// Commencer l'envoie
		for (int j = 0; j < repFactor; j++) {

			for (int i = 0; i < fragments.length; i++) {
				File frag = new File(fragments[i]);
				int numServer = Math.floorMod(i + j, Config.WORKERS.size());
				int tailleFrag = (int) frag.length();
				String fileName = fname + ".frag." + i;
				String FileToSend = Utils.multiString(",", 64 - (fileName.length())) + fileName;

				// Envoyer les informations (taille,nomDuFrag,cmd_write) au DataNode
				String cmd = Utils.multiString(",", 16 - ("CMD_WRITE".length())) + "CMD_WRITE";
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
				bufReader.close();
				// ajouter le fragment et le nom du DataNode à la liste listeDesFrag
				Pair<Integer, ClusterNode> indice = new Pair<>(i, Config.WORKERS.get(numServer));
				listeDesFrag.add(indice);
			}

			if (Config.WORKERS.size()<2){
				break;
			}
		}

		// Supprimer les fragments locals
		File directory = new File(Config.TMP_PATH);
		for (File f : directory.listFiles()) {
			System.out.print("Suppression du fragment " + f);
			f.delete();
			System.out.println(" ...OK");
		}

		// Ajouter le fichier et la liste de ses fragments et leurs emplacements à la
		// liste du NameNode
		nameNode.put(fname, listeDesFrag);
	}

	// La Methode HdfsRead
	public void HdfsRead(String hdfsFname, String localFSDestFname) throws Exception {

		// Creer le fichier local
		String fname = localFSDestFname;
		File file = File.createTempFile(fname, "");
		FileOutputStream stream = new FileOutputStream(fname);
		int len;
		int tailleFichier;

		// Commencer
		for (int i = 0; i < nameNode.get(hdfsFname).size(); i++) {

			// Recuperer le numero du DataNode suivant
			int numServer = Math.floorMod(i, Config.WORKERS.size());
			InputStream input = inputStreams.get(numServer);

			// envoie des informations sur le fichier au DataNode
			// format: 0...0,,,nomFichier,,,..CMD_READ
			int tailleFrag = 0;
			String nomFrag = hdfsFname + ".frag." + i;
			String FileToSend = Utils.multiString(",", 64 - (nomFrag.length())) + nomFrag;
			String cmd = Utils.multiString(",", 16 - ("CMD_READ".length())) + "CMD_READ";
			byte[] bytes = (Utils.multiString("0", (int) (16 - (tailleFrag + "").length())) + tailleFrag + FileToSend
					+ cmd).getBytes();
			outputStreams.get(numServer).write(bytes, 0, bytes.length);

			// Recevoir les infos sur le fichier
			bytes = new byte[96];
			len = input.read(bytes);
			String[] infos = Utils.splitStr(Utils.bytes2String(bytes), ",");

			tailleFichier = Integer.parseInt(infos[0]);
			int tailleRestante = tailleFichier;
			bytes = new byte[Math.min(512, tailleRestante)];

			// Recevoir le fichier
			while (tailleRestante != 0) {
				len = input.read(bytes);
				// recuperation du fichier
				stream.write(bytes, 0, len);
				tailleRestante = tailleRestante - len;
				bytes = new byte[Math.min(512, tailleRestante)];
			}
			System.out.println("--Reception du fichier " + i + " ...OK");
		}
		Utils.toFichier(file, stream.toString());
		if ((new File(fname)).exists()) {
			System.out.println("--Concatenation des fichiers reçu ... " + "\n--Fichier " + fname + " crée");
		} else {
			System.out.println("fichier fichier n'a pas été crée");
		}
		stream.close();

	}

	public void HdfsList(String... hdfsFnames) throws Exception {
		if (hdfsFnames.length == 0)
			System.out.println(nameNode.getInfoFiles());
		else
			for (String hdfsFname : hdfsFnames) {
				System.out.println(nameNode.getInfoFile(hdfsFname));
			}
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
		switch (args[0]) {
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
			else if (args[1].equals("nb"))
			fmt = Format.Type.NB;
			else {
				usage();
				return;
			}
			hc.HdfsWrite(fmt, args[2], 1);
			break;
		case "read":
			hc.HdfsRead(args[1], args[2]);
			break;
		case "delete":
			hc.HdfsDelete(args[1]);
			break;
		case "ls":
			String[] hdfsFnames = Arrays.copyOfRange(args, 1, args.length);
			hc.HdfsList(hdfsFnames);
			break;
		default:
			usage();
		}
		hc.closeServers();
	}

}
