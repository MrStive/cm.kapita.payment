package com.domeni.kapita.payment.repositories;

import com.domeni.kapita.payment.domain.transaction.Transaction;
import com.domeni.kapita.payment.domain.transaction.TransactionId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, TransactionId> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
