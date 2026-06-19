package com.domeni.kapita.payment.domain.exception;

public class PaymentProviderException extends DomainException {
    public PaymentProviderException(String provider, String message) {
        super(String.format("Error from payment provider %s: %s", provider, message));
    }

    public PaymentProviderException(String provider, String message, Throwable cause) {
        super(String.format("Error from payment provider %s: %s", provider, message), cause);
    }
}
