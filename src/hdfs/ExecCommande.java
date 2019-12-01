package hdfs;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public class ExecCommande extends Thread {
	
	private HdfsServerIt serveur;
	private Commande cmd;
	private String fichier;
	private long taille;
	public ExecCommande(HdfsServerIt s,String f,Commande cmd,long taillefrag) {
		super();
		this.taille = taillefrag;
		this.serveur = s;
		this.cmd = cmd;
		this.fichier = f;
	}
	@Override
	public void run() {
		try {
			serveur.execCmd(cmd, fichier, taille);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
