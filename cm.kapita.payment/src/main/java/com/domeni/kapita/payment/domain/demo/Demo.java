package com.domeni.kapita.payment.domain.demo;

import com.domeni.kapita.domain.core.SoftDeleteJpaEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.jspecify.annotations.Nullable;

@FieldNameConstants
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_demo")
public class Demo extends SoftDeleteJpaEntity<DemoId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "c_id"))
    private DemoId id = new DemoId();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "c_name"))
    private @Nullable DemoName name;

    @Builder
    public Demo(DemoId id, DemoName name) {
        this.id = id != null ? id : new DemoId();
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Demo demo)) {
            return false;
        }
        return Objects.equals(id, demo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
