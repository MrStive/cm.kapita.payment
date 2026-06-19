package com.domeni.kapita.payment.repositories;

import com.domeni.kapita.payment.domain.account.Account;
import com.domeni.kapita.payment.domain.account.AccountId;
import com.domeni.kapita.payment.domain.account.AccountType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, AccountId> {
    Optional<Account> findByType(AccountType type);
    Optional<Account> findByOwnerId(String ownerId);
}
