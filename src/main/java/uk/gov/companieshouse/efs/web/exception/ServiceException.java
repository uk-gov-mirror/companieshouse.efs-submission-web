package uk.gov.companieshouse.efs.web.exception;

/**
 * The class {@code ServiceException} is intended to abstract lower level
 * exceptions from being propagated up the call stack.
 */
public class ServiceException extends RuntimeException {

    /**
     * Constructs a new {@code ServiceException} with a custom message and the specified
     * cause.
     *
     * @param message a custom message
     * @param cause the cause
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code ServiceException} with a custom message.
     *
     * @param message a custom message
     */
    public ServiceException(final String message) {
        super(message);
    }
}
