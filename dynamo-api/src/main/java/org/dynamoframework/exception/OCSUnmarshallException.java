package org.dynamoframework.exception;

public class OCSUnmarshallException extends OCSRuntimeException {

    private static final long serialVersionUID = 6144119769190157855L;

    public OCSUnmarshallException() {
        super();
    }

    public OCSUnmarshallException(Throwable cause) {
        super(cause);
    }

    public OCSUnmarshallException(String message) {
        super(message);
    }

    public OCSUnmarshallException(String message, Throwable cause) {
        super(message, cause);
    }
}
