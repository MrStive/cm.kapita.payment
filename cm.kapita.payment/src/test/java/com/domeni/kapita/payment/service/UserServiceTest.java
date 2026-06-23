package com.domeni.kapita.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.domeni.kapita.payment.domain.exception.InvalidUserPayloadException;
import com.domeni.kapita.payment.domain.user.UserCreationData;
import com.domeni.kapita.payment.domain.user.UserFactory;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserFactory userFactory;

    @InjectMocks private UserService userService;

    @Captor private ArgumentCaptor<UserCreationData> userCreationDataCaptor;

    @Test
    void createUserShouldNormalizeAndDelegateToFactoryTest() {
        // Given
        UUID userId = UUID.randomUUID();
        UserCreationData input =
                UserCreationData.builder()
                        .id(userId)
                        .name("  john.doe  ")
                        .firstname("  John  ")
                        .lastname(" Doe ")
                        .email(" john.doe@example.com ")
                        .build();

        // When
        userService.createUser(input);

        // Then
        then(userFactory).should().create(userCreationDataCaptor.capture());
        // Normalization now happens inside the domain objects created by the factory,
        // so the service passes the raw data.
        assertThat(userCreationDataCaptor.getValue()).isEqualTo(input);
    }

    @Test
    void createUserWhenInputIsNullShouldThrowInvalidUserPayloadExceptionTest() {
        // When / Then
        assertThatThrownBy(() -> userService.createUser(null))
                .isInstanceOf(InvalidUserPayloadException.class)
                .hasMessage("user creation payload is required and must be valid");

        verifyNoInteractions(userFactory);
    }

    @Test
    void createUserWhenIdIsMissingShouldThrowInvalidUserPayloadExceptionTest() {
        // Given
        UserCreationData input = UserCreationData.builder().name("john.doe").build();

        // When / Then
        assertThatThrownBy(() -> userService.createUser(input))
                .isInstanceOf(InvalidUserPayloadException.class)
                .hasMessage("user creation payload is required and must be valid");

        verifyNoInteractions(userFactory);
    }

    @Test
    void createUserWhenNameIsBlankShouldThrowInvalidUserPayloadExceptionTest() {
        // Given
        UserCreationData input =
                UserCreationData.builder().id(UUID.randomUUID()).name("   ").build();

        // When / Then
        assertThatThrownBy(() -> userService.createUser(input))
                .isInstanceOf(InvalidUserPayloadException.class)
                .hasMessage("user creation payload is required and must be valid");

        verifyNoInteractions(userFactory);
    }
}
