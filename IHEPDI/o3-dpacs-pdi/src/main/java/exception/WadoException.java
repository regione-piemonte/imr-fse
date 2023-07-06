package exception;

public class WadoException extends Exception {

	
	private static final long serialVersionUID = 239216578798489746L;

	public WadoException() {
	}

	public WadoException(String message) {
		super(message);
	}

	public WadoException(Throwable cause) {
		super(cause);
	}

	public WadoException(String message, Throwable cause) {
		super(message, cause);
	}

	public WadoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
