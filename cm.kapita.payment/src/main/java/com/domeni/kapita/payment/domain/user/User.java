package com.domeni.kapita.payment.domain.user;

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
@Table(name = "t_user")
public class User extends SoftDeleteJpaEntity<UserId> {

  @EmbeddedId
  @AttributeOverride(name = "value", column = @Column(name = "c_id"))
  private UserId id = new UserId();

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "c_name"))
  private @Nullable UserName name;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "c_firstname"))
  private @Nullable UserFirstName firstname;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "c_lastname"))
  private @Nullable UserLastName lastname;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "c_email"))
  private @Nullable UserEmail email;

  @Builder
  public User(
      UserId id,
      @Nullable UserName name,
      @Nullable UserFirstName firstname,
      @Nullable UserLastName lastname,
      @Nullable UserEmail email) {
    this.id = id != null ? id : new UserId();
    this.name = name;
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User user)) {
      return false;
    }
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
