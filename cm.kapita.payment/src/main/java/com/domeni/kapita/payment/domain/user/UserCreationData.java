package com.domeni.kapita.payment.domain.user;

import java.util.UUID;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UserCreationData(
        @Nullable UUID id,
        @Nullable String name,
        @Nullable String firstname,
        @Nullable String lastname,
        @Nullable String email) {}
