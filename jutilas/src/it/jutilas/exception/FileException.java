package it.jutilas.exception;

/**
 * Class for file exception
 * @author Andrea Serra
 *
 */
public class FileException extends java.lang.Exception {
	private static final long serialVersionUID = -6811904031273058744L;

	public FileException() {
	}

	public FileException(String message) {
		super(message);
	}

	public FileException(Throwable cause) {
		super(cause);
	}

	public FileException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
