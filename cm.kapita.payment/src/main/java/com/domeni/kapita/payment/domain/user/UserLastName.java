package com.domeni.kapita.payment.domain.user;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode
@Getter
public class UserLastName {
    private String value;

    @SuppressWarnings("NullAway.Init")
    protected UserLastName() {
    }

    public UserLastName(String value) {
        this.value = StringNormalizer.normalize(value);
    }
}
