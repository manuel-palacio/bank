package com.palace.smallapp.bank.transfer;

import com.google.common.eventbus.EventBus;
import com.palace.smallapp.bank.*;
import com.palace.smallapp.bank.account.AccountRepository;
import com.palace.smallapp.bank.account.event.AccountsBalanceUpdated;
import com.palace.smallapp.bank.account.model.Account;
import com.palace.smallapp.bank.transfer.event.TransferCreatedEvent;
import com.palace.smallapp.bank.transfer.model.Leg;
import com.palace.smallapp.bank.transfer.model.Transfer;
import com.palace.smallapp.bank.transfer.query.TransferQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.palace.smallapp.bank.account.AccountPrecondition.checkNotNull;

@Component
public class TransferServiceImpl implements TransferService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EventBus eventPublisher;

    @Autowired
    private TransferQueryService transferQueryService;

    @Autowired
    TaskExecutor taskExecutor;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    public void transferFunds(TransferFundsRequest transferRequest) throws InsufficientFundsException,
            AccountNotFoundException, AccountClosedException {

        if (isNewTransferRequest(transferRequest.getTransactionRef())) {
            Transfer transfer = Transfer.create(transferRequest);

            List<Account> updatedAccounts = updateAccountsBalance(transfer);

            transferRepository.save(transfer);

            publishTransferEvents(transfer, updatedAccounts);
        }
    }

    private boolean isNewTransferRequest(String transactionRef) {
        return transferRepository.findByTransactionRef(transactionRef) == null;
    }

    //In a real CQRS system the events are published to interested parties by the domain (aggregate root)
    private void publishTransferEvents(final Transfer transfer, final List<Account> updatedAccounts) {
        eventPublisher.post(new TransferCreatedEvent(transfer));
        eventPublisher.post(new AccountsBalanceUpdated(updatedAccounts));
    }

    private List<Account> updateAccountsBalance(Transfer transfer) {
        List<Account> updatedAccounts = new ArrayList<Account>();
        for (Leg leg : transfer.getLegs()) {
            Account account = checkNotNull(accountRepository.findByAccountRef(leg.getAccountRef()));
            account.updateBalanceWithLeg(leg);
            updatedAccounts.add(accountRepository.save(account));   //the account repository joins the transfer funds tx automatically
        }
        return updatedAccounts;
    }

    @Override
    public List<Transaction> findTransactions(String accountRef) throws AccountNotFoundException {
        return transferQueryService.findTransactions(accountRef);
    }
}
