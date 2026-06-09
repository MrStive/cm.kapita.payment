package com.domeni.kapita.payment.service;

import com.domeni.kapita.payment.domain.exception.InvalidUserPayloadException;
import com.domeni.kapita.payment.domain.user.UserCreationData;
import com.domeni.kapita.payment.domain.user.UserFactory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserFactory userFactory;

    @Transactional
    public void createUser(@Nullable UserCreationData data) {
        if (data == null) {
            throw new InvalidUserPayloadException("user creation payload is required");
        }
        UUID id = data.id();
        if (id == null) {
            throw new InvalidUserPayloadException("user creation payload is invalid");
        }

        String normalizedName = normalizeRequired(data.name());
        if (normalizedName == null) {
            throw new InvalidUserPayloadException("user creation payload is invalid");
        }

        userFactory.create(
                UserCreationData.builder()
                        .id(id)
                        .name(normalizedName)
                        .firstname(normalizeOptional(data.firstname()))
                        .lastname(normalizeOptional(data.lastname()))
                        .email(normalizeOptional(data.email()))
                        .build());
    }

    private @Nullable String normalizeRequired(@Nullable String value) {
        String normalizedValue = normalizeOptional(value);
        return normalizedValue == null || normalizedValue.isBlank() ? null : normalizedValue;
    }

    private @Nullable String normalizeOptional(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
