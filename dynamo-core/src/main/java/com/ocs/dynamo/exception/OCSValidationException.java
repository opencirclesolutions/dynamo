package com.ocs.dynamo.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception indicating that one or more validation errors have occurred
 * 
 * @author bas.rutten
 */
public class OCSValidationException extends OCSRuntimeException {

	private static final long serialVersionUID = 8242893962990806889L;

	private final List<String> errors;

	/**
	 * Constructor
	 * 
	 * @param error
	 */
	public OCSValidationException(String error) {
		this.errors = new ArrayList<>();
		this.errors.add(error);
	}

	/**
	 * Constructor
	 * 
	 * @param errors
	 */
	public OCSValidationException(List<String> errors) {
		this.errors = errors;
	}

	public List<String> getErrors() {
		return errors;
	}

}
