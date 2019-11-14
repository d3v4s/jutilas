package it.jutilas.exception;

/**
 * Class for network exception
 * @author Andrea Serra
 *
 */
public class NetworkExcepiton extends Exception {
	private static final long serialVersionUID = 6698297296335302417L;

	public NetworkExcepiton() {
	}

	public NetworkExcepiton(String message) {
		super(message);
	}

	public NetworkExcepiton(Throwable cause) {
		super(cause);
	}

	public NetworkExcepiton(String message, Throwable cause) {
		super(message, cause);
	}

	public NetworkExcepiton(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
