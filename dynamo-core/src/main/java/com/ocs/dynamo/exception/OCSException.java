package com.ocs.dynamo.exception;

/**
 * Base class for OCS checked exceptions
 * 
 * @author bas.rutten
 */
public class OCSException extends Exception {

    private static final long serialVersionUID = -4040969323105084394L;

    public OCSException() {
    }

    /**
     * Constructor
     * 
     * @param message
     */
    public OCSException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param message
     * @param cause
     */
    public OCSException(String message, Throwable cause) {
        super(message, cause);
    }
}
