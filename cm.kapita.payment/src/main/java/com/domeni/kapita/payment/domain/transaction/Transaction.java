package com.domeni.kapita.payment.domain.transaction;

import com.domeni.kapita.domain.core.SoftDeleteJpaEntity;
import com.domeni.kapita.payment.domain.account.AccountId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_transaction")
@SuppressWarnings({"all", "NullAway.Init"})
public class Transaction extends SoftDeleteJpaEntity<TransactionId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "c_id"))
    private TransactionId id = new TransactionId();

    @Column(name = "c_idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "c_status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "c_type")
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "c_from_account_id"))
    private AccountId fromAccountId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "c_to_account_id"))
    private AccountId toAccountId;

    @Column(name = "c_amount")
    @Convert(converter = com.domeni.kapita.payment.jpa.converter.MonetaryAmountConverter.class)
    private javax.money.MonetaryAmount amount;

    @Column(name = "c_reason")
    private String reason;

    @Column(name = "c_description")
    private String description;

    @Builder
    public Transaction(
            TransactionId id,
            String idempotencyKey,
            TransactionStatus status,
            TransactionType type,
            AccountId fromAccountId,
            AccountId toAccountId,
            javax.money.MonetaryAmount amount,
            String reason,
            String description) {
        this.id = id != null ? id : new TransactionId();
        this.idempotencyKey = idempotencyKey;
        this.status = status != null ? status : TransactionStatus.PENDING;
        this.type = type;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.reason = reason;
        this.description = description;
    }

    public void complete() {
        if (this.status == TransactionStatus.COMPLETED) {
            return;
        }
        if (this.status == TransactionStatus.FAILED) {
            throw new com.domeni.kapita.payment.domain.exception.IllegalTransactionStateException(this.id, this.status, "complete");
        }
        this.status = TransactionStatus.COMPLETED;
    }

    public void fail() {
        if (this.status == TransactionStatus.COMPLETED) {
            throw new com.domeni.kapita.payment.domain.exception.IllegalTransactionStateException(this.id, this.status, "fail");
        }
        this.status = TransactionStatus.FAILED;
    }
}
