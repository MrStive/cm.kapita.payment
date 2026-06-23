package com.domeni.kapita.payment.repositories;

import com.domeni.kapita.payment.domain.payment.PaymentIntent;
import com.domeni.kapita.payment.domain.payment.PaymentIntentId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, PaymentIntentId> {
    Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey);
}
