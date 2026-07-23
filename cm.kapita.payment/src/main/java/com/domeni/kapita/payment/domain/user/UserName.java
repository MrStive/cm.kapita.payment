package com.domeni.kapita.payment.domain.user;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@Builder
@Getter
public class UserName {
  private String value;

  public UserName(String value) {
    this.value = StringNormalizer.normalize(value);
  }
}
