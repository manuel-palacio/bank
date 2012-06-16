package com.palace.smallapp.bank.account;


import com.palace.smallapp.bank.AccountNotFoundException;

public class AccountPrecondition {

    public static <T> T checkNotNull(T ref) {
        if (ref == null) {
            throw new AccountNotFoundException();
        }

        return ref;
    }
}
