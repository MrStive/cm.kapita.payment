package com.domeni.kapita.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.domeni.kapita.payment.domain.account.Account;
import com.domeni.kapita.payment.domain.account.AccountId;
import com.domeni.kapita.payment.domain.account.AccountType;
import com.domeni.kapita.payment.repositories.AccountRepository;
import com.domeni.kapita.payment.repositories.ProviderTransactionRepository;
import com.domeni.kapita.payment.repositories.TransactionRepository;
import com.domeni.kapita.payment.service.model.CreateTransferRequest;
import com.domeni.kapita.payment.service.model.CreatedTransferRequest;
import com.domeni.kapita.payment.service.ports.PaymentGateway;
import com.domeni.kapita.payment.service.ports.PaymentInitiationRequest;
import com.domeni.kapita.payment.service.ports.PaymentStatusPublisher;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private ProviderTransactionRepository providerTransactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private PaymentGateway paymentGateway;
    @Mock private PaymentStatusPublisher paymentStatusPublisher;

    @InjectMocks private PaymentService paymentService;

    @Test
    void shouldInitiatePaymentSuccessfully() {
        // Given
        CreateTransferRequest request =
                new CreateTransferRequest(
                        "id1", new BigDecimal("1000"), "XAF", "699000000", "MONETBIL", "", "user1", "desc");
        when(transactionRepository.findByIdempotencyKey("id1")).thenReturn(Optional.empty());

        Account platformAccount =
                Account.builder()
                        .type(AccountType.INTERNAL)
                        .id(new AccountId())
                        .build();
        Account userAccount =
                Account.builder()
                        .type(AccountType.EXTERNAL)
                        .ownerId("user1")
                        .id(new AccountId())
                        .build();
        when(accountRepository.findByType(AccountType.INTERNAL)).thenReturn(Optional.of(platformAccount));
        when(accountRepository.findByOwnerId("user1")).thenReturn(Optional.of(userAccount));

        when(paymentGateway.initiatePayment(any(PaymentInitiationRequest.class)))
                .thenReturn(Optional.of("http://monetbil.com/pay"));

        // When
        CreatedTransferRequest result = paymentService.processDeposit(request);

        // Then
        verify(transactionRepository).save(any());
        verify(paymentGateway).initiatePayment(any(PaymentInitiationRequest.class));
        assertThat(result.paymentUrl()).isEqualTo("http://monetbil.com/pay");
    }
}
