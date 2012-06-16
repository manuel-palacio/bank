package com.palace.smallapp.bank.account.event;

import com.google.common.eventbus.Subscribe;
import com.palace.smallapp.bank.config.EventHandler;
import com.palace.smallapp.bank.Money;
import com.palace.smallapp.bank.account.model.Account;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Publishes new balance for updated/created accounts
 */
@EventHandler
@Component
public class AccountEventHandler {

    @Resource(name = "accounts")
    Map<String, Money> accounts;

    @Subscribe
    public void accountCreated(AccountCreatedEvent event) {
        Account account = event.getAccount();
        accounts.put(account.getAccountRef(), new Money(account.getBalance(), account.getCurrency()));
    }

    @Subscribe
    public void accountsUpdated(AccountsBalanceUpdated event) {
        List<Account> updatedAccounts = event.getAccounts();

        for (Account account : updatedAccounts) {
            accounts.put(account.getAccountRef(), new Money(account.getBalance(), account.getCurrency()));
        }
    }
}
