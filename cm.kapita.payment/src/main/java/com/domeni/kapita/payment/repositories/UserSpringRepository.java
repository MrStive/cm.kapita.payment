package com.domeni.kapita.payment.repositories;

import com.domeni.kapita.payment.domain.user.User;
import com.domeni.kapita.payment.domain.user.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSpringRepository extends JpaRepository<User, UserId> {}
