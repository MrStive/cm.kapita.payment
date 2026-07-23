package com.domeni.kapita.payment.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.domeni.kapita.generated.payment.event.dto.EmailAddressDTO;
import com.domeni.kapita.generated.payment.event.dto.UserCreatedEventDTO;
import com.domeni.kapita.payment.domain.user.UserCreationData;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserEventMapperTest {

  private final UserEventMapper userEventMapper = Mappers.getMapper(UserEventMapper.class);

  @Test
  void mapUserCreatedEventShouldReturnUserCreationDataTest() {
    // Given
    UUID userId = UUID.randomUUID();
    UserCreatedEventDTO input =
        new UserCreatedEventDTO()
            .id(userId)
            .username("john.doe")
            .firstname("John")
            .lastname("Doe")
            .email(new EmailAddressDTO().email("john.doe@example.com"));

    // When
    UserCreationData result = userEventMapper.map(input);

    // Then
    assertThat(result)
        .isEqualTo(
            UserCreationData.builder()
                .id(userId)
                .name("john.doe")
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .build());
  }

  @Test
  void mapUserCreatedEventWhenInputIsNullShouldReturnNullTest() {
    // When
    UserCreationData result = userEventMapper.map((UserCreatedEventDTO) null);

    // Then
    assertThat(result).isNull();
  }

  @Test
  void mapEmailAddressWhenInputIsNullShouldReturnNullTest() {
    // When
    String result = userEventMapper.map((EmailAddressDTO) null);

    // Then
    assertThat(result).isNull();
  }
}
