package com.domeni.kapita.payment.service.model;

import com.domeni.kapita.payment.domain.transaction.Transaction;

public record CreatedTransferRequest(
        String transactionId, String paymentUrl, Transaction transaction) {
    public static CreatedTransferRequest of(
            String transactionId, String paymentUrl, Transaction transaction) {
        return new CreatedTransferRequest(transactionId, paymentUrl, transaction);
    }
}
