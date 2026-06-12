package com.domeni.kapita.payment.service.events.model;

public record PaymentStatusEvent(String transactionId, String status, MoneyDTO amount) {}
