package com.domeni.kapita.payment.domain.user;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.jspecify.annotations.Nullable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@Builder
@Getter
public class UserFirstName {
    private @Nullable String value;

    @SuppressWarnings("NullAway.Init")
    public UserFirstName(@Nullable String value) {
        this.value = StringNormalizer.normalize(value);
    }
}
