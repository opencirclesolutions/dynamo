package com.ocs.dynamo.exception;

/**
 * Base class for any runtime exceptions. Must always be used instead of a plain java.lang.RuntimeException.
 * Subclass when appropriate
 * @author bas.rutten
 *
 */
public class OCSRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -6263372299801009820L;

	public OCSRuntimeException() {
	}
	
	public OCSRuntimeException(String message) {
		super(message);
	}
	
	public OCSRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
