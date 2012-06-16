package com.palace.smallapp.bank.transfer;

import com.palace.smallapp.bank.transfer.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Spring JPA implementation(less) repository with basic CRUD ops
 */
public interface TransferRepository extends JpaRepository<Transfer, Integer> {

    Transfer findByTransactionRef(String transactionRef);

}
