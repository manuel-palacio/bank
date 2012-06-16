package com.palace.smallapp.bank.account;


import com.palace.smallapp.bank.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Spring JPA implementation(less) repository
 */
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Account findByAccountRef(String accountRef);
}
