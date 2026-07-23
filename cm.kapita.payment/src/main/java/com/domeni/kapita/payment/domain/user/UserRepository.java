package com.domeni.kapita.payment.domain.user;

import java.util.Optional;

public interface UserRepository {
  User save(User value);

  Optional<User> findById(UserId id);
}
