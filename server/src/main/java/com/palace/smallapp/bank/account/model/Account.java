package com.palace.smallapp.bank.account.model;

import com.palace.smallapp.bank.*;
import com.palace.smallapp.bank.transfer.model.Leg;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Entity
public class Account {

    @NotNull
    private String accountRef;

    private BigDecimal balance;

    private Currency currency;

    private boolean isClosed;

    @Id
    protected String id = UUID.randomUUID().toString();

    @Version
    protected int version;

    private Account() {
    }

    public Account(String accountRef, Money amount) {
        this.accountRef = accountRef;
        this.currency = amount.getCurrency();
        this.balance = amount.getAmount();
    }

    public boolean isClosed() {
        return isClosed;
    }

    public String getAccountRef() {
        return accountRef;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    private void setBalance(BigDecimal balance) {
        BigDecimal result = this.balance.add(balance);

        if (result.signum() == -1) {
            throw new InsufficientFundsException("Account balance cannot be negative");
        }

        this.balance = result;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return accountRef.equals(account.accountRef);

    }

    @Override
    public int hashCode() {
        return accountRef.hashCode();
    }

    public void updateBalanceWithLeg(Leg leg) {
        if (isClosed()) {
            throw new AccountClosedException(accountRef);
        }
        if (leg.getCurrency().equals(currency)) {
            setBalance(leg.getAmount());
        } else {
            throw new IllegalTransferRequestException("Cannot update balance in another currency");
        }
    }
}
