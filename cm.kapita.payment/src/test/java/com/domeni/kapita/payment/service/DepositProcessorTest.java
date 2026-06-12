package com.domeni.kapita.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.domeni.kapita.payment.repositories.AccountRepository;
import com.domeni.kapita.payment.repositories.ProviderTransactionRepository;
import com.domeni.kapita.payment.repositories.TransactionRepository;
import com.domeni.kapita.payment.service.gateway.MonetbilGateway;
import com.domeni.kapita.payment.service.model.CreateTransferRequest;
import java.util.Optional;

import com.domeni.kapita.payment.service.model.CreatedTransferRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepositProcessorTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private ProviderTransactionRepository providerTransactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private MonetbilGateway monetbilGateway;

    @InjectMocks private DepositProcessor depositProcessor;

    @Test
    void shouldInitiatePaymentSuccessfully() {
        // Given
        CreateTransferRequest request =
                new CreateTransferRequest(
                        "id1", "1000", "XAF", "699000000", "MONETBIL", "http://return", "desc");
        when(transactionRepository.findByIdempotencyKey("id1")).thenReturn(Optional.empty());

        com.domeni.kapita.payment.domain.account.Account platformAccount =
                com.domeni.kapita.payment.domain.account.Account.builder()
                        .type(com.domeni.kapita.payment.domain.account.AccountType.INTERNAL)
                        .id(new com.domeni.kapita.payment.domain.account.AccountId())
                        .build();
        when(accountRepository.findAll()).thenReturn(java.util.List.of(platformAccount));

        com.domeni.kapita.generated.monetbil.dto.WidgetPaymentResponseDto monetbilRes =
                new com.domeni.kapita.generated.monetbil.dto.WidgetPaymentResponseDto();
        monetbilRes.setPaymentUrl("http://monetbil.com/pay");
        when(monetbilGateway.initiatePayment(any(), any())).thenReturn(monetbilRes);

        // When
        CreatedTransferRequest result = depositProcessor.process(request);

        // Then
        verify(transactionRepository).save(any());
        verify(monetbilGateway).initiatePayment(any(), any());
        assertThat(result.paymentUrl()).isEqualTo("http://monetbil.com/pay");
    }
}
