package com.domeni.kapita.payment.repositories;

import com.domeni.kapita.payment.domain.payment.PaymentIntentId;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttempt;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttemptId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderAttemptRepository
    extends JpaRepository<ProviderAttempt, ProviderAttemptId> {
  Optional<ProviderAttempt> findByPaymentIntentId(PaymentIntentId paymentIntentId);
}
