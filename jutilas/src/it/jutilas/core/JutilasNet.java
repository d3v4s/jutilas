package it.jutilas.core;

import java.io.IOException;
import java.net.ServerSocket;

import it.jutilas.exception.NetworkExcepiton;

public class JutilasNet {
	private static JutilasNet networkAS;

	private JutilasNet() {
	}

	/* singleton */
	public static JutilasNet getInstance() {
		networkAS = (networkAS == null) ? new JutilasNet() : networkAS;
		return (networkAS = (networkAS == null) ? new JutilasNet() : networkAS);
	}

	/* metodo che crea e ritorna un ServerSocket della prima porta disponibile di quelle passate in ingresso */
	public ServerSocket createServerSocket(int... listPort) throws NetworkExcepiton {
		for (int i : listPort)
			try {
				return new ServerSocket(i);
			} catch (IOException e) {
				continue;
			}
		
		throw new NetworkExcepiton("Nessuna porta, di quelle passate come argomento, e' diponibile");
	}

	/* metodo che controlla se la porta locale e' disponibile */
	public boolean isAvaiblePort(int port) {
		try {
			new ServerSocket(port).close();;
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
