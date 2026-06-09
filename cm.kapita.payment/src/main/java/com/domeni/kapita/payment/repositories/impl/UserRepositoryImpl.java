package com.domeni.kapita.payment.repositories.impl;

import com.domeni.kapita.payment.domain.user.User;
import com.domeni.kapita.payment.domain.user.UserId;
import com.domeni.kapita.payment.domain.user.UserRepository;
import com.domeni.kapita.payment.repositories.UserSpringRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserSpringRepository userSpringRepository;

    @Override
    public User save(User value) {
        return userSpringRepository.save(value);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userSpringRepository.findById(id);
    }
}
