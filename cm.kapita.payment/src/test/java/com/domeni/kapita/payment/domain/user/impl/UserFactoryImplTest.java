package com.domeni.kapita.payment.domain.user.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.domeni.kapita.payment.domain.user.User;
import com.domeni.kapita.payment.domain.user.UserCreationData;
import com.domeni.kapita.payment.domain.user.UserId;
import com.domeni.kapita.payment.domain.user.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFactoryImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserFactoryImpl userFactory;

    @Test
    void createShouldBuildAndPersistUserFromCreationDataTest() {
        // Given
        UUID userId = UUID.randomUUID();
        UserCreationData input =
                UserCreationData.builder()
                        .id(userId)
                        .name("john.doe")
                        .firstname("John")
                        .lastname("Doe")
                        .email("john.doe@example.com")
                        .build();
        given(userRepository.findById(any(UserId.class))).willReturn(Optional.empty());
        User persistedUser = new User();
        given(userRepository.save(any(User.class))).willReturn(persistedUser);

        // When
        User result = userFactory.create(input);

        // Then
        assertThat(result).isSameAs(persistedUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().findById(any(UserId.class));
        then(userRepository).should().save(userCaptor.capture());

        User userToSave = userCaptor.getValue();
        assertThat(userToSave.getId()).isNotNull();
        assertThat(userToSave.getId().toUUID()).isEqualTo(userId);
        assertThat(userToSave.getName()).isNotNull();
        assertThat(userToSave.getName().getValue()).isEqualTo("john.doe");
        assertThat(userToSave.getFirstname()).isNotNull();
        assertThat(userToSave.getFirstname().getValue()).isEqualTo("John");
        assertThat(userToSave.getLastname()).isNotNull();
        assertThat(userToSave.getLastname().getValue()).isEqualTo("Doe");
        assertThat(userToSave.getEmail()).isNotNull();
        assertThat(userToSave.getEmail().getValue()).isEqualTo("john.doe@example.com");
    }

    @Test
    void createWhenUserAlreadyExistsShouldReturnExistingUserWithoutSavingTest() {
        // Given
        UUID userId = UUID.randomUUID();
        UserCreationData input =
                UserCreationData.builder()
                        .id(userId)
                        .name("john.doe")
                        .firstname("John")
                        .lastname("Doe")
                        .email("john.doe@example.com")
                        .build();
        User existingUser = new User();
        given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(existingUser));

        // When
        User result = userFactory.create(input);

        // Then
        assertThat(result).isSameAs(existingUser);
        then(userRepository).should().findById(any(UserId.class));
        then(userRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void createWhenUserCreationDataIsNullShouldThrowNullPointerExceptionTest() {
        // When / Then
        assertThatThrownBy(() -> userFactory.create(null)).isInstanceOf(NullPointerException.class);

        verifyNoInteractions(userRepository);
    }
}
