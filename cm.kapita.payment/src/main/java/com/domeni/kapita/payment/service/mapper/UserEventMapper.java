package com.domeni.kapita.payment.service.mapper;

import com.domeni.kapita.generated.payment.event.dto.EmailAddressDTO;
import com.domeni.kapita.generated.payment.event.dto.UserCreatedEventDTO;
import com.domeni.kapita.payment.domain.user.UserCreationData;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserEventMapper {
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "username")
  @Mapping(target = "firstname", source = "firstname")
  @Mapping(target = "lastname", source = "lastname")
  @Mapping(target = "email", source = "email")
  @Nullable UserCreationData map(@Nullable UserCreatedEventDTO source);

  default @Nullable String map(@Nullable EmailAddressDTO emailAddressDTO) {
    return Optional.ofNullable(emailAddressDTO).map(EmailAddressDTO::getEmail).orElse(null);
  }
}
