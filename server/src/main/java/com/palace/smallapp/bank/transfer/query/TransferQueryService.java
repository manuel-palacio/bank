package com.palace.smallapp.bank.transfer.query;

import com.hazelcast.core.MultiMap;
import com.palace.smallapp.bank.Transaction;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.palace.smallapp.bank.account.AccountPrecondition.checkNotNull;

/**
 * Represents query side of transfer information
 */
@Service
public class TransferQueryService {

    @Resource(name = "transactions")
    private MultiMap<String, Transaction> transactionMap;

    public List<Transaction> findTransactions(String accountRef) {

        return new ArrayList<Transaction>(transactionMap.get(accountRef));
    }
}
