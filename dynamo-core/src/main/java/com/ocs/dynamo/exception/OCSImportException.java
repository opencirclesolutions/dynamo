package com.ocs.dynamo.exception;

/**
 * An exception used to indicate that something went wrong during an import
 * 
 * @author bas.rutten
 */
public class OCSImportException extends OCSRuntimeException {

    private static final long serialVersionUID = -8031113801985156724L;

    public OCSImportException() {
    }

    public OCSImportException(String message) {
        super(message);
    }

    public OCSImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
