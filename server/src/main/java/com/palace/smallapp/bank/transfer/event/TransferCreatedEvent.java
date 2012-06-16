package com.palace.smallapp.bank.transfer.event;

import com.palace.smallapp.bank.transfer.model.Transfer;


public class TransferCreatedEvent {


    private final Transfer transfer;

    public TransferCreatedEvent(Transfer transfer) {
        this.transfer = transfer;
    }

    public Transfer getTransfer() {
        return transfer;
    }
}
