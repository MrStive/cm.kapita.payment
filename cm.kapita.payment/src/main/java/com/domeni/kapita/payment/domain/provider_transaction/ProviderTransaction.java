package com.domeni.kapita.payment.domain.provider_transaction;

import com.domeni.kapita.domain.core.SoftDeleteJpaEntity;
import com.domeni.kapita.payment.domain.transaction.TransactionId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
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
@Table(name = "t_provider_transaction")
@SuppressWarnings({"all", "NullAway.Init"})
public class ProviderTransaction extends SoftDeleteJpaEntity<ProviderTransactionId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "c_id"))
    private ProviderTransactionId id = new ProviderTransactionId();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "c_transaction_id"))
    private TransactionId transactionId;

    @Column(name = "c_provider")
    private String provider;

    @Column(name = "c_external_id")
    private String externalId;

    @Column(name = "c_payment_url")
    private String paymentUrl;

    @Column(name = "c_phone")
    private String phone;

    @Column(name = "c_status")
    @Enumerated(EnumType.STRING)
    private ProviderTransactionStatus status;

    @Column(name = "c_raw_payload")
    private String rawPayload;

    @Builder
    public ProviderTransaction(
            ProviderTransactionId id,
            TransactionId transactionId,
            String provider,
            String externalId,
            String paymentUrl,
            String phone,
            ProviderTransactionStatus status,
            String rawPayload) {
        this.id = id != null ? id : new ProviderTransactionId();
        this.transactionId = transactionId;
        this.provider = provider;
        this.externalId = externalId;
        this.paymentUrl = paymentUrl;
        this.phone = phone;
        this.status = status;
        this.rawPayload = rawPayload;
    }
}
