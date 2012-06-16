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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static com.jayway.awaitility.Awaitility.await;
import static com.palace.smallapp.bank.MoneyUtils.toMoney;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Main integration test demonstrating the functionality of the bank system. In order to qualify for
 * submitting the smallapp solution this test must pass without failures.
 *
 * @version 1.0
 */
@ContextConfiguration(classes = {ApplicationConfig.class, EventConfig.class,
        PersistenceConfig.class, QueryConfiguration.class}, loader = AnnotationConfigContextLoader.class)
public class BankTest extends AbstractTestNGSpringContextTests {
    static final String SAVINGS_ACCOUNT_1 = "MyAccounts:A:EUR";

    static final String SPENDING_ACCOUNT_1 = "MyAccounts:B:EUR";

    static final String SAVINGS_ACCOUNT_2 = "MyAccounts:C:SEK";

    static final String SPENDING_ACCOUNT_2 = "MyAccounts:D:SEK";

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;


    @Test
    public void transferMoney() throws Exception {
        String randomSuffix = UUID.randomUUID().toString();

        accountService.createAccount(SAVINGS_ACCOUNT_1 + randomSuffix, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1 + randomSuffix, toMoney("0.00 EUR"));

        await().atMost(5, SECONDS).until(balanceIsPopulated(SAVINGS_ACCOUNT_1 + randomSuffix, toMoney("1000.00 EUR")));
        await().atMost(5, SECONDS).until(balanceIsPopulated(SPENDING_ACCOUNT_1 + randomSuffix, toMoney("0.00 EUR")));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1 + randomSuffix), toMoney("1000.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1 + randomSuffix), toMoney("0.00 EUR"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T1").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1 + randomSuffix).amount(toMoney("-5.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1 + randomSuffix).amount(toMoney("5.00 EUR"))
                .build());

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T2").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1 + randomSuffix).amount(toMoney("-10.50 EUR"))
                .accountRef(SPENDING_ACCOUNT_1 + randomSuffix).amount(toMoney("10.50 EUR"))
                .build());

        await().atMost(5, SECONDS).until(balanceIsPopulated(SAVINGS_ACCOUNT_1 + randomSuffix, toMoney("984.50 EUR")));
        await().atMost(5, SECONDS).until(balanceIsPopulated(SPENDING_ACCOUNT_1 + randomSuffix, toMoney("15.50 EUR")));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1 + randomSuffix), toMoney("984.50 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1 + randomSuffix), toMoney("15.50 EUR"));

        List<Transaction> t1 = transferService.findTransactions(SAVINGS_ACCOUNT_1 + randomSuffix);
        assertNotNull(t1);
        assertEquals(t1.size(), 2);
        assertEquals(t1.iterator().next().getLegs().size(), 1);

        List<Transaction> t2 = transferService.findTransactions(SPENDING_ACCOUNT_1 + randomSuffix);
        assertNotNull(t2);
        assertEquals(t2.size(), 2);
        assertEquals(t2.iterator().next().getLegs().size(), 1);
    }

    private Callable<Boolean> balanceIsPopulated(final String accountRef, final Money money) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return accountService.getBalance(accountRef).equals(money);
            }
        };
    }

    @Test
    public void transferMoneyUsingMultiLeggedTransaction() {
        String randomSuffix = UUID.randomUUID().toString();

        accountService.createAccount(SAVINGS_ACCOUNT_1 + randomSuffix, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1 + randomSuffix, toMoney("0.00 EUR"));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1 + randomSuffix), toMoney("1000.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1 + randomSuffix), toMoney("0.00 EUR"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T3").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1 + randomSuffix).amount(toMoney("-5.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1 + randomSuffix).amount(toMoney("5.00 EUR"))
                .accountRef(SAVINGS_ACCOUNT_1 + randomSuffix).amount(toMoney("-10.50 EUR"))
                .accountRef(SPENDING_ACCOUNT_1 + randomSuffix).amount(toMoney("10.50 EUR"))
                .accountRef(SAVINGS_ACCOUNT_1 + randomSuffix).amount(toMoney("-2.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1 + randomSuffix).amount(toMoney("1.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1 + randomSuffix).amount(toMoney("1.00 EUR"))
                .build());

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1 + randomSuffix),
                toMoney("982.50 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1 + randomSuffix),
                toMoney("17.50 EUR"));
    }

    @Test
    public void transferMoneyUsingMultiLeggedAndMultiCurrencyTransactions() {
        accountService.createAccount(SAVINGS_ACCOUNT_1, toMoney("1000.00 EUR"));
        accountService.createAccount(SPENDING_ACCOUNT_1, toMoney("0.00 EUR"));
        accountService.createAccount(SAVINGS_ACCOUNT_2, toMoney("1000.00 SEK"));
        accountService.createAccount(SPENDING_ACCOUNT_2, toMoney("0.00 SEK"));

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_2), toMoney("1000.00 SEK"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_2), toMoney("0.00 SEK"));

        transferService.transferFunds(TransferFundsRequest.builder()
                .transactionRef("T4").transactionType("testing")
                .accountRef(SAVINGS_ACCOUNT_1).amount(toMoney("-5.00 EUR"))
                .accountRef(SPENDING_ACCOUNT_1).amount(toMoney("5.00 EUR"))
                .accountRef(SAVINGS_ACCOUNT_2).amount(toMoney("-10.50 SEK"))
                .accountRef(SPENDING_ACCOUNT_2).amount(toMoney("10.50 SEK"))
                .build());

        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_1), toMoney("995.00 EUR"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_1), toMoney("5.00 EUR"));
        assertEquals(accountService.getBalance(SAVINGS_ACCOUNT_2), toMoney("989.50 SEK"));
        assertEquals(accountService.getBalance(SPENDING_ACCOUNT_2), toMoney("10.50 SEK"));
    }
}
