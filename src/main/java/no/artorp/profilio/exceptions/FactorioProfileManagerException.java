package no.artorp.profilio.exceptions;

public class FactorioProfileManagerException extends Exception {

	private static final long serialVersionUID = -5458969547347159332L;

	public FactorioProfileManagerException(String message) {
		super(message);
	}

	public FactorioProfileManagerException(Throwable cause) {
		super(cause);
	}

	public FactorioProfileManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public FactorioProfileManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
