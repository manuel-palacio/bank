package com.palace.smallapp.bank.account.query;

import com.palace.smallapp.bank.Money;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Simple facade to the query side of account information
 */
@Service
public class AccountQueryService {

    @Resource(name = "accounts")
    private Map<String, Money> accounts;

    public Money getBalance(String accountRef) {
        return accounts.get(accountRef);
    }
}
