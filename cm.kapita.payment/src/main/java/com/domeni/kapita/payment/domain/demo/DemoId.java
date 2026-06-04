package com.domeni.kapita.payment.domain.demo;

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
public class DemoId implements Serializable {
    private String value = UUID.randomUUID().toString();

    public DemoId(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DemoId demoId)) {
            return false;
        }
        return Objects.equals(value, demoId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
