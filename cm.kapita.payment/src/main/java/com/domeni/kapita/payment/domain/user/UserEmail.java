package com.domeni.kapita.payment.domain.user;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NonNull;

@Embeddable
@Getter
public class UserEmail {
    private String value;

    @SuppressWarnings("NullAway.Init")
    protected UserEmail() {}

    public UserEmail(@NonNull String value) {
        this.value = StringNormalizer.normalize(value);
    }
}
