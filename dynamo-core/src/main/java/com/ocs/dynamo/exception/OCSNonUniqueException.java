package com.ocs.dynamo.exception;

/**
 * An exception that is used to indicates that an entity cannot be added or
 * update because it would violate a uniqueness constraint
 * 
 * @author bas.rutten
 */
public class OCSNonUniqueException extends RuntimeException {

	private static final long serialVersionUID = -6263372299801009820L;

	public OCSNonUniqueException() {
	}

	public OCSNonUniqueException(String message) {
		super(message);
	}

	public OCSNonUniqueException(String message, Throwable cause) {
		super(message, cause);
	}
}
