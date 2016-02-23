package com.ocs.dynamo.exception;

/**
 * An exception used to indicate that something went wrong with regard to a
 * (long-running) calculation
 * 
 * @author bas.rutten
 *
 */
public class OCSCalculationException extends OCSRuntimeException {

	private static final long serialVersionUID = -6208518328383294989L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public OCSCalculationException(String message) {
		super(message);
	}
}
