package jutilas.core;

import java.io.IOException;
import java.net.ServerSocket;

import jutilas.exception.NetworkExcepiton;

/**
 * Class with utils for network
 * @author Andrea Serra
 *
 */
public class JutilasNet {
	private static JutilasNet networkAS;

	/* CONSTRUCTOR */
	private JutilasNet() {
	}

	/* SINGLETON */
	public static JutilasNet getInstance() {
		return (networkAS = (networkAS == null) ? new JutilasNet() : networkAS);
	}

	/* metodo che crea e ritorna un ServerSocket della prima porta disponibile di quelle passate in ingresso */
	/**
	 * method that create a local socket with first available port
	 * @param portList to create a socket
	 * @return local socket
	 * @throws NetworkExcepiton
	 */
	public ServerSocket createServerSocket(int... portList) throws NetworkExcepiton {
		for (int i : portList) {
			try {
				return new ServerSocket(i);
			} catch (IOException e) {
				continue;
			}
		}

		throw new NetworkExcepiton("No port is available");
	}

	/* metodo che controlla se la porta locale e' disponibile */
	/**
	 * method who check if the local port is available
	 * @param port to check
	 * @return true if available, else false
	 */
	public boolean isAvailablePort(int port) {
		try {
			new ServerSocket(port).close();;
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
