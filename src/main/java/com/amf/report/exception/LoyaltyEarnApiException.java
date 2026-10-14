package com.amf.report.exception;

/** Thrown when the external loyaltyEarn TMF API call fails. */
public class LoyaltyEarnApiException extends RuntimeException {

    public LoyaltyEarnApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
