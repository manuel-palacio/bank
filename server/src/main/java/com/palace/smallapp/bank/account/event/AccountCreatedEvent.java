package com.palace.smallapp.bank.account.event;

import com.palace.smallapp.bank.account.model.Account;


public class AccountCreatedEvent {
    private final Account account;

    public AccountCreatedEvent(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
