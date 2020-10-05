package uk.gov.companieshouse.efs.web.exception;

public class UrlEncodingException extends RuntimeException {

    public UrlEncodingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}