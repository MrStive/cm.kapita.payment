package com.domeni.kapita.payment.domain.exception;

import com.domeni.kapita.payment.domain.transaction.TransactionId;
import com.domeni.kapita.payment.domain.transaction.TransactionStatus;

public class IllegalTransactionStateException extends DomainException {
    public IllegalTransactionStateException(TransactionId id, TransactionStatus current, String action) {
        super(String.format("Cannot %s transaction %s because it is in %s state", action, id.getValue(), current));
    }
}
