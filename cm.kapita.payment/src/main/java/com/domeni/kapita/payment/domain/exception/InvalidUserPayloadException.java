package com.domeni.kapita.payment.domain.exception;

public class InvalidUserPayloadException extends RuntimeException {
    public InvalidUserPayloadException(String message) {
        super(message);
    }
}
