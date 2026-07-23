package com.domeni.kapita.payment.jpa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;
import javax.money.MonetaryAmount;
import org.javamoney.moneta.Money;

@Converter
public class MonetaryAmountConverter implements AttributeConverter<MonetaryAmount, String> {
  private static final String SEPARATOR = ";";

  @Override
  public String convertToDatabaseColumn(MonetaryAmount attribute) {
    if (attribute == null) {
      return null;
    }

    BigDecimal value = attribute.getNumber().numberValueExact(BigDecimal.class);
    return attribute.getCurrency().getCurrencyCode() + SEPARATOR + value.toPlainString();
  }

  @Override
  public MonetaryAmount convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return null;
    }

    String[] tokens = dbData.split(SEPARATOR, 2);
    if (tokens.length != 2 || tokens[0].isBlank() || tokens[1].isBlank()) {
      throw new IllegalArgumentException("invalid monetary amount value: " + dbData);
    }

    return Money.of(new BigDecimal(tokens[1]), tokens[0]);
  }
}
