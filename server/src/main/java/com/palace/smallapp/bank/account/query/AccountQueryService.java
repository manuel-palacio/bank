package com.palace.smallapp.bank.account.query;

import com.palace.smallapp.bank.Money;


public interface AccountQueryService {
    Money getBalance(String accountRef);
}
