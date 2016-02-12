package nl.ocs.exception;

/**
 * An exception that indicates that a security-related issues has occurrred
 * @author bas.rutten
 *
 */
public class OCSSecurityException extends OCSRuntimeException {

	private static final long serialVersionUID = 5973822987694088860L;

	public OCSSecurityException(String message) {
		super(message);
	}
}
