package com.palace.smallapp.bank.account.event;

import com.palace.smallapp.bank.account.model.Account;

import java.util.List;


public class AccountsBalanceUpdated {

    private final List<Account> accounts;

    public AccountsBalanceUpdated(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}