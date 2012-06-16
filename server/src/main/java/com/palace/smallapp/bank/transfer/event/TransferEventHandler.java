package com.palace.smallapp.bank.transfer.event;

import ch.lambdaj.group.Group;
import com.google.common.eventbus.Subscribe;
import com.hazelcast.core.MultiMap;
import com.palace.smallapp.bank.config.EventHandler;
import com.palace.smallapp.bank.Money;
import com.palace.smallapp.bank.Transaction;
import com.palace.smallapp.bank.TransactionLeg;
import com.palace.smallapp.bank.transfer.model.Leg;
import com.palace.smallapp.bank.transfer.model.Transfer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.*;


/**
 * Publish to query/report side
 */
@EventHandler
@Component
public class TransferEventHandler {

    @Resource(name = "transactions")
    MultiMap<String, Transaction> transactionMap;  //associates key with multiple values so we can get all transactions for given account


    @Subscribe
    public void transferCreated(TransferCreatedEvent event) {

        Transfer transfer = event.getTransfer();

        Group<Leg> group = group(transfer.getLegs(), by(on(Leg.class).getAccountRef()));

        for (String accountRef : group.keySet()) {

            List<Leg> legs = group.find(accountRef);
            List<TransactionLeg> transactionLegs = new ArrayList<TransactionLeg>();
            for (Leg next : legs) {
                transactionLegs.add(new TransactionLeg(next.getAccountRef(), new Money(next.getAmount(), next.getCurrency())));
            }

            transactionMap.put(accountRef, new Transaction(transfer.getTransactionRef(),transfer.getTransactionType(),
                                    transfer.getBookingDate(),transactionLegs));
        }
    }
}
