package com.domeni.kapita.payment.service.model;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {}
