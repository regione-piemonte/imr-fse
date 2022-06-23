package it.csi.dmass.service.rest.error;

public class ValidationException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3494171832811079788L;

	public ValidationException(String msg) {
        super(msg);
    }
}
