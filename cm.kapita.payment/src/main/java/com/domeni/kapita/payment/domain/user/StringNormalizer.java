package com.domeni.kapita.payment.domain.user;

import org.jspecify.annotations.Nullable;

public class StringNormalizer {
    public static @Nullable String normalize(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
