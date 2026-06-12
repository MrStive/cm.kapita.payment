package com.domeni.kapita.payment.domain.transaction;

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
public class TransactionId implements Serializable {
    private String value = UUID.randomUUID().toString();

    public TransactionId(UUID value) {
        this.value = Objects.requireNonNull(value, "value").toString();
    }

    public UUID toUUID() {
        return UUID.fromString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionId transactionId)) return false;
        return Objects.equals(value, transactionId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
