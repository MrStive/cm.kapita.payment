package com.domeni.kapita.payment.domain.user;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode
@Getter
public class UserFirstName {
    private String value;

    @SuppressWarnings("NullAway.Init")
    protected UserFirstName() {
    }

    public UserFirstName(String value) {
        this.value = Objects.requireNonNull(StringNormalizer.normalize(value));
    }
}
