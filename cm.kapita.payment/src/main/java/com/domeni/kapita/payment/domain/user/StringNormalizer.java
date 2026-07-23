package com.domeni.kapita.payment.domain.user;

import org.jspecify.annotations.NonNull;

public class StringNormalizer {
  public static String normalize(@NonNull String value) {
    return value.trim();
  }
}
