package com.domeni.kapita.payment.service.events.model;

import java.math.BigDecimal;

public record MoneyDTO(BigDecimal amount, String currency) {}
