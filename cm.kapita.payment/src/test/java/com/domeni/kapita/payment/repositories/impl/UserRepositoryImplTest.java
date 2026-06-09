package com.domeni.kapita.payment.repositories.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.domeni.kapita.payment.domain.user.User;
import com.domeni.kapita.payment.domain.user.UserId;
import com.domeni.kapita.payment.repositories.UserSpringRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @InjectMocks private UserRepositoryImpl objectUnderTest;

    @Mock private UserSpringRepository userSpringRepository;

    @Test
    void saveShouldDelegateToSpringRepositoryTest() {
        // Given
        User user = mock(User.class);
        User persistedUser = mock(User.class);
        given(userSpringRepository.save(user)).willReturn(persistedUser);

        // When
        User result = objectUnderTest.save(user);

        // Then
        assertThat(result).isSameAs(persistedUser);
        then(userSpringRepository).should().save(user);
    }

    @Test
    void findByIdShouldDelegateToSpringRepositoryTest() {
        // Given
        UserId userId = new UserId(UUID.randomUUID());
        User user = mock(User.class);
        given(userSpringRepository.findById(userId)).willReturn(Optional.of(user));

        // When
        Optional<User> result = objectUnderTest.findById(userId);

        // Then
        assertThat(result).containsSame(user);
        then(userSpringRepository).should().findById(userId);
    }
}
