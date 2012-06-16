package com.palace.smallapp.bank.account;


import com.google.common.eventbus.EventBus;
import com.palace.smallapp.bank.AccountNotFoundException;
import com.palace.smallapp.bank.AccountService;
import com.palace.smallapp.bank.Money;
import com.palace.smallapp.bank.account.event.AccountCreatedEvent;
import com.palace.smallapp.bank.account.model.Account;
import com.palace.smallapp.bank.account.query.AccountQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountQueryService accountQueryService;

    @Autowired
    private EventBus eventPublisher;

    @Override
    public void createAccount(String accountRef, Money amount) {

        if (accountRepository.findByAccountRef(accountRef) == null) {
            Account account = new Account(accountRef, amount);

            accountRepository.save(account);
            //publish to query side
            eventPublisher.post(new AccountCreatedEvent(account));
        }
    }

    @Override
    public Money getBalance(String accountRef) throws AccountNotFoundException {
        return accountQueryService.getBalance(accountRef);
    }
}
