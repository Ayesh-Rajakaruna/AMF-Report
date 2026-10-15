package com.amf.report.exception;

public class HttpServiceException extends RuntimeException {
    public HttpServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
