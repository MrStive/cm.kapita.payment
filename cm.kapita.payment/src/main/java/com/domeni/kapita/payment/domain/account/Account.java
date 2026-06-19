package com.domeni.kapita.payment.domain.account;

import com.domeni.kapita.domain.core.SoftDeleteJpaEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
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
@Table(name = "t_account")
@SuppressWarnings({"all", "NullAway.Init"})
public class Account extends SoftDeleteJpaEntity<AccountId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "c_id"))
    private AccountId id = new AccountId();

    @Column(name = "c_type")
    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Column(name = "c_owner_id")
    private String ownerId;

    @Column(name = "c_balance")
    @jakarta.persistence.Convert(converter = com.domeni.kapita.payment.jpa.converter.MonetaryAmountConverter.class)
    private javax.money.MonetaryAmount balance;

    @Column(name = "c_number")
    private Long number;

    @Column(name = "c_provider")
    private String provider;

    @Builder
    public Account(AccountId id, AccountType type, String ownerId, javax.money.MonetaryAmount balance, Long number, String provider) {
        this.id = id != null ? id : new AccountId();
        this.type = type;
        this.ownerId = ownerId;
        this.balance = balance;
        this.number = number;
        this.provider = provider;
    }
}
