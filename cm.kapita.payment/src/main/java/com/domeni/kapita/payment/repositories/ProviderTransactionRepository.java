package com.domeni.kapita.payment.repositories;

import com.domeni.kapita.payment.domain.provider_transaction.ProviderTransaction;
import com.domeni.kapita.payment.domain.provider_transaction.ProviderTransactionId;
import com.domeni.kapita.payment.domain.transaction.TransactionId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderTransactionRepository
        extends JpaRepository<ProviderTransaction, ProviderTransactionId> {
    Optional<ProviderTransaction> findByTransactionId(TransactionId transactionId);
}
