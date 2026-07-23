package com.domeni.kapita.payment.service;

import com.domeni.kapita.payment.domain.exception.InvalidUserPayloadException;
import com.domeni.kapita.payment.domain.user.UserCreationData;
import com.domeni.kapita.payment.domain.user.UserFactory;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserFactory userFactory;

  @Transactional
  public void createUser(@Nullable UserCreationData data) {
    if (data == null || data.id() == null || data.name() == null || data.name().isBlank()) {
      throw new InvalidUserPayloadException("user creation payload is required and must be valid");
    }

    userFactory.create(data);
  }
}
