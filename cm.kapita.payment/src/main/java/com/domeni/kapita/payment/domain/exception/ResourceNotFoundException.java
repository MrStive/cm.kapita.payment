package com.domeni.kapita.payment.domain.exception;

public class ResourceNotFoundException extends DomainException {
  public ResourceNotFoundException(String resourceName, String identifier) {
    super(String.format("%s with identifier %s not found", resourceName, identifier));
  }
}
