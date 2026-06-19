package com.domeni.kapita.payment.service.ports;

import com.domeni.kapita.payment.domain.transaction.Transaction;

public interface PaymentStatusPublisher {
    void publish(Transaction transaction);
}
