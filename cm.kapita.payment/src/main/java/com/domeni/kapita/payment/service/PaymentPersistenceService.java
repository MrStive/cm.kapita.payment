package com.domeni.kapita.payment.service;

import com.domeni.kapita.payment.domain.payment.PaymentIntent;
import com.domeni.kapita.payment.domain.provider_attempt.ProviderAttempt;
import com.domeni.kapita.payment.repositories.PaymentIntentRepository;
import com.domeni.kapita.payment.repositories.ProviderAttemptRepository;
import com.domeni.kapita.payment.service.ports.PaymentStatusPublisher;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("NullAway.Init")
public class PaymentPersistenceService {
  private final PaymentIntentRepository paymentIntentRepository;
  private final ProviderAttemptRepository providerAttemptRepository;
  private final PaymentStatusPublisher paymentStatusPublisher;

  @Transactional(readOnly = true)
  public Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey) {
    return paymentIntentRepository.findByIdempotencyKey(idempotencyKey);
  }

  @Transactional(readOnly = true)
  public Optional<ProviderAttempt> findAttempt(PaymentIntent paymentIntent) {
    return providerAttemptRepository.findByPaymentIntentId(paymentIntent.getId());
  }

  @Transactional
  public LocalPayment create(PaymentIntent paymentIntent, ProviderAttempt providerAttempt) {
    paymentIntentRepository.saveAndFlush(paymentIntent);
    providerAttemptRepository.saveAndFlush(providerAttempt);
    return new LocalPayment(paymentIntent, providerAttempt);
  }

  @Transactional
  public LocalPayment markPaymentPending(
      PaymentIntent paymentIntent,
      ProviderAttempt providerAttempt,
      String providerReference,
      String paymentUrl,
      String rawResponse) {
    ProviderAttempt reloadedAttempt =
        providerAttemptRepository.findById(providerAttempt.getId()).orElse(providerAttempt);
    PaymentIntent reloadedIntent =
        paymentIntentRepository.findById(paymentIntent.getId()).orElse(paymentIntent);

    if (reloadedIntent.isFinal() || reloadedAttempt.isFinal()) {
      return new LocalPayment(reloadedIntent, reloadedAttempt);
    }

    reloadedAttempt.markPending(providerReference, paymentUrl, rawResponse);
    reloadedIntent.markPaymentPending();
    providerAttemptRepository.saveAndFlush(reloadedAttempt);
    paymentIntentRepository.saveAndFlush(reloadedIntent);
    return new LocalPayment(reloadedIntent, reloadedAttempt);
  }

  @Transactional
  public void markInitiationFailed(
      PaymentIntent paymentIntent, ProviderAttempt providerAttempt, String reason) {
    providerAttempt.markFailed(reason, null); // rawPayload not available at initiation failure
    paymentIntent.markFailed(reason);
    providerAttemptRepository.save(providerAttempt);
    paymentIntentRepository.save(paymentIntent);
    paymentStatusPublisher.publish(paymentIntent, providerAttempt);
  }

  public record LocalPayment(PaymentIntent paymentIntent, ProviderAttempt providerAttempt) {}
}
