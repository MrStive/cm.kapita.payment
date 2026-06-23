package com.domeni.kapita.payment.domain.provider_attempt;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ProviderAttemptId implements Serializable {
    private String value = UUID.randomUUID().toString();

    public ProviderAttemptId(UUID value) {
        this.value = Objects.requireNonNull(value, "value").toString();
    }

    public UUID toUUID() {
        return UUID.fromString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderAttemptId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
