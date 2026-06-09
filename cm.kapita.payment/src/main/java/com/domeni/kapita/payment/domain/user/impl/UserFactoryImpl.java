package com.domeni.kapita.payment.domain.user.impl;

import com.domeni.kapita.payment.domain.user.User;
import com.domeni.kapita.payment.domain.user.UserCreationData;
import com.domeni.kapita.payment.domain.user.UserEmail;
import com.domeni.kapita.payment.domain.user.UserFactory;
import com.domeni.kapita.payment.domain.user.UserFirstName;
import com.domeni.kapita.payment.domain.user.UserId;
import com.domeni.kapita.payment.domain.user.UserLastName;
import com.domeni.kapita.payment.domain.user.UserName;
import com.domeni.kapita.payment.domain.user.UserRepository;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFactoryImpl implements UserFactory {
    private final UserRepository userRepository;

    @Override
    public User create(UserCreationData userCreationData) {
        UUID id = Objects.requireNonNull(userCreationData.id(), "id");
        String name = Objects.requireNonNull(userCreationData.name(), "name");
        UserId userId = new UserId(id);
        return userRepository
                .findById(userId)
                .map(
                        existingUser -> {
                            log.info(
                                    "User with id={} already exists, skipping creation",
                                    userId.getValue());
                            return existingUser;
                        })
                .orElseGet(
                        () ->
                                userRepository.save(
                                        User.builder()
                                                .id(userId)
                                                .name(new UserName(name))
                                                .firstname(
                                                        mapFirstName(userCreationData.firstname()))
                                                .lastname(mapLastName(userCreationData.lastname()))
                                                .email(mapEmail(userCreationData.email()))
                                                .build()));
    }

    private @Nullable UserFirstName mapFirstName(@Nullable String value) {
        return value == null ? null : new UserFirstName(value);
    }

    private @Nullable UserLastName mapLastName(@Nullable String value) {
        return value == null ? null : new UserLastName(value);
    }

    private @Nullable UserEmail mapEmail(@Nullable String value) {
        return value == null ? null : new UserEmail(value);
    }
}
