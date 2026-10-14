package com.amf.report.exception;

/** Thrown when an outbound HTTP call made through HttpService fails. */
public class HttpServiceException extends RuntimeException {

    public HttpServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
