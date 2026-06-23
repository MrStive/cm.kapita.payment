package com.domeni.kapita.payment.domain.exception;

public class IdempotencyConflictException extends DomainException {
    public IdempotencyConflictException(String idempotencyKey) {
        super("Idempotency key already exists with a different payment payload: " + idempotencyKey);
    }
}
