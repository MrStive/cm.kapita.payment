package com.domeni.kapita.payment.domain.user;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode
@Getter
public class UserLastName {
    private String value;

    public UserLastName(String value) {
        this.value = StringNormalizer.normalize(value);
    }
}
