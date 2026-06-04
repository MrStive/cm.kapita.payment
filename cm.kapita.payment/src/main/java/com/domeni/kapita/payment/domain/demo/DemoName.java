package com.domeni.kapita.payment.domain.demo;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class DemoName implements Serializable {
    private @Nullable String value;

    public DemoName(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DemoName demoName)) {
            return false;
        }
        return Objects.equals(value, demoName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
