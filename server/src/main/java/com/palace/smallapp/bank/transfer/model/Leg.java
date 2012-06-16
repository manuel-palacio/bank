package com.palace.smallapp.bank.transfer.model;


import com.palace.smallapp.bank.Money;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;


@Entity
public class Leg  {

    private BigDecimal amount;

    private Currency currency;

    private String accountRef;

    @Id
    protected String id = UUID.randomUUID().toString();

    @Version
    protected int version;

    private Leg() {
    }

    public Leg(Money money, String accountRef) {
        this.amount = money.getAmount();
        this.currency = money.getCurrency();
        this.accountRef = accountRef;
    }

    public String getAccountRef() {
        return accountRef;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
