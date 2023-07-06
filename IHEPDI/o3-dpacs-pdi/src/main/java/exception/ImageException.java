package exception;

public class ImageException extends Exception {

	private static final long serialVersionUID = 2568975118333098861L;

	public ImageException() {
	}

	public ImageException(String message) {
		super(message);
	}

	public ImageException(Throwable cause) {
		super(cause);
	}

	public ImageException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
