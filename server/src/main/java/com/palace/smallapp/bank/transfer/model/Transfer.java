package com.palace.smallapp.bank.transfer.model;


import com.palace.smallapp.bank.IllegalTransferRequestException;
import com.palace.smallapp.bank.TransactionLeg;
import com.palace.smallapp.bank.TransferFundsRequest;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

import static ch.lambdaj.Lambda.*;

/**
 * Domain class representing a money transfer request
 */
@Entity
public class Transfer implements Comparable<Date> {

    private String transactionRef;

    private String transactionType;

    private Date bookingDate;

    @Id
    protected String id = UUID.randomUUID().toString();

    @Version
    protected int version;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final Set<Leg> legs = new HashSet<Leg>();


    private Transfer() {
    }

    private Transfer(TransferFundsRequest transferFundsRequest) {

        this.transactionRef = transferFundsRequest.getTransactionRef();
        this.transactionType = transferFundsRequest.getTransactionType();
        this.bookingDate = transferFundsRequest.getBookingDate();

        for (TransactionLeg transactionLeg : transferFundsRequest.getLegs()) {
            addLeg(new Leg(transactionLeg.getAmount(), transactionLeg.getAccountRef()));
        }

        validateTransferIsBetweenAtLeast2Accounts();

        validateThatSumOfAllCreditsAndDebitsIsZero();
    }


    private void validateTransferIsBetweenAtLeast2Accounts() {
        List<String> accountRefs = collect(legs, on(Leg.class).getAccountRef());
        if (accountRefs.size() < 2) {
            throw new IllegalTransferRequestException("Transfer must be done between at least two accounts");
        }
    }

    private void validateThatSumOfAllCreditsAndDebitsIsZero() {

        BigDecimal sumAllLegs = sum(legs, on(Leg.class).getAmount());
        if (sumAllLegs.byteValueExact() != 0) {
            throw new IllegalTransferRequestException("Luca has a problem with this transfer funds request");
        }
    }

    private void addLeg(Leg leg) {
        legs.add(leg);
    }


    public String getTransactionRef() {
        return transactionRef;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Date getBookingDate() {
        return new Date(bookingDate.getTime());
    }

    public Set<Leg> getLegs() {
        return Collections.unmodifiableSet(legs);
    }

    @Override
    public int compareTo(Date date) {
        return this.bookingDate.compareTo(date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transfer that = (Transfer) o;

        if (!bookingDate.equals(that.bookingDate)) return false;
        if (!transactionRef.equals(that.transactionRef)) return false;
        if (!transactionType.equals(that.transactionType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = transactionRef.hashCode();
        result = 31 * result + transactionType.hashCode();
        result = 31 * result + bookingDate.hashCode();
        return result;
    }

    public static Transfer create(TransferFundsRequest transferRequest) {
        return new Transfer(transferRequest);
    }
}
