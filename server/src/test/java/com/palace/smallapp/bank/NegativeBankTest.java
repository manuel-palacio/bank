package com.palace.smallapp.bank;

import com.palace.smallapp.bank.config.ApplicationConfig;
import com.palace.smallapp.bank.config.EventConfig;
import com.palace.smallapp.bank.config.PersistenceConfig;
import com.palace.smallapp.bank.config.QueryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

import static com.jayway.awaitility.Awaitility.await;
import static com.palace.smallapp.bank.MoneyUtils.toMoney;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@ContextConfiguration(classes = {ApplicationConfig.class, EventConfig.class,
        PersistenceConfig.class, QueryConfiguration.class}, loader = AnnotationConfigContextLoader.class)
public class NegativeBankTest extends AbstractTestNGSpringContextTests {

    static final String SAVINGS_ACCOUNT_1 = "MyAccounts:A:EUR";

    static final String SPENDING_ACCOUNT_1 = "MyAccounts:B:EUR";

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;


    @Test(expectedExceptions = InsufficientFundsException.class)
    public void cannot_overdraw_account() {

        accountService.createAccount(SAVINGS_ACCOUNT_1, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1, toMoney("0.00 EUR"));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1), toMoney("1000.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1), toMoney("0.00 EUR"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T3").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-5000.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("5000.00 EUR"))
                .build());

        assertTrue(transferService.findTransactions(SAVINGS_ACCOUNT_1).isEmpty());
        assertTrue(transferService.findTransactions(SPENDING_ACCOUNT_1).isEmpty());

    }

    @Test(expectedExceptions = IllegalTransferRequestException.class)
    public void accounting_principle_holds() throws Exception {

        accountService.createAccount(SAVINGS_ACCOUNT_1, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1, toMoney("0.00 EUR"));

        await().atMost(5, SECONDS).until(balanceIsPopulated(SAVINGS_ACCOUNT_1, toMoney("1000.00 EUR")));
        await().atMost(5, SECONDS).until(balanceIsPopulated(SPENDING_ACCOUNT_1, toMoney("0.00 EUR")));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1), toMoney("1000.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1), toMoney("0.00 EUR"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T3").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-50.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("51.00 EUR"))
                .build());

        assertTrue(transferService.findTransactions(SAVINGS_ACCOUNT_1).isEmpty());
        assertTrue(transferService.findTransactions(SPENDING_ACCOUNT_1).isEmpty());


    }

    @Test(expectedExceptions = AccountNotFoundException.class)
    public void transfer_to_non_existing_account_fails() {

        accountService.createAccount(SAVINGS_ACCOUNT_1, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1, toMoney("0.00 EUR"));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1), toMoney("1000.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1), toMoney("0.00 EUR"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T3").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-1000.00 EUR"))
                .accountRef("foo").amount(toMoney("1000.00 EUR"))
                .build());

        assertTrue(transferService.findTransactions(SAVINGS_ACCOUNT_1).isEmpty());

    }

    @Test(expectedExceptions = IllegalTransferRequestException.class)
    public void legs_for_account_have_to_be_in_same_currency() {

        accountService.createAccount(SAVINGS_ACCOUNT_1, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1, toMoney("0.00 EUR"));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1), toMoney("1000.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1), toMoney("0.00 EUR"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T3").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-5.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("5.00 EUR"))
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-10.50 SEK"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("10.50 EUR"))
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-2.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("1.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("1.00 SEK"))
                .build());

        assertTrue(transferService.findTransactions(SAVINGS_ACCOUNT_1).isEmpty());

    }

    @Test(expectedExceptions = AccountNotFoundException.class)
    public void request_transaction_for_non_existing_account_fails() {
        transferService.findTransactions("foo");
    }


    private Callable<Boolean> balanceIsPopulated(final String accountRef, final Money money) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return accountService.getBalance(accountRef).equals(money);
            }
        };
    }

}
