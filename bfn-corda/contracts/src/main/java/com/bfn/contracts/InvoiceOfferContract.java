package com.bfn.contracts;

import com.bfn.states.InvoiceOfferState;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

// ************
// * Contract *
// ************

public class InvoiceOfferContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = InvoiceOfferContract.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(InvoiceOfferContract.class);

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException{

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 InvoiceOfferContract: verify starting" +
                " ..... \uD83E\uDD6C \uD83E\uDD6C ");
        if (tx.getOutputStates().size() != 1) {
            throw new IllegalArgumentException("One output InvoiceOfferState is required");
        }
        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Only one command allowed");
        }
        Command command = tx.getCommand(0);
        if ((command.getValue() instanceof MakeOffer || command.getValue() instanceof BuyOffer)) {
        } else {
            throw new IllegalArgumentException("Only MakeOffer or BuyOffer command allowed");
        }
        List<PublicKey> requiredSigners = command.getSigners();
        logger.info(" \uD83D\uDD34  \uD83D\uDD34 Required signers: " + requiredSigners.size());
        for (PublicKey key: requiredSigners) {
            logger.info(" \uD83D\uDD34 Required signer publicKey: ".concat(key.toString()));
        }
        ContractState contractState = tx.getOutput(0);
        if (!(contractState instanceof InvoiceOfferState)) {
            throw new IllegalArgumentException("Output state must be InvoiceOfferState");
        }
        InvoiceOfferState invoiceState = (InvoiceOfferState)contractState;
        if (invoiceState.getSupplier() == null) {
            throw new IllegalArgumentException("Supplier is required");
        }
        if (invoiceState.getInvestor() == null) {
            throw new IllegalArgumentException("Investor is required");
        }
        if (invoiceState.getCustomer() == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (invoiceState.getOwner() == null) {
            throw new IllegalArgumentException("Owner is definitely required");
        }
        if (invoiceState.getOfferDate() == null) {
            throw new IllegalArgumentException("Offer date is required");
        }
        if (invoiceState.getOfferDate().getTime() > new Date().getTime()) {
            throw new IllegalArgumentException("Offer date cannot be in the future");
        }


        //check signatures of all parties
        PublicKey supplierPublicKey = invoiceState.getSupplier().getHost().getOwningKey();
        logger.info(" \uD83D\uDD34 Supplier publicKey: ".concat(supplierPublicKey.toString()));
        if (!requiredSigners.contains(supplierPublicKey)) {
            throw new IllegalArgumentException("Supplier Party must sign");
        }
        PublicKey investorPublicKey = invoiceState.getInvestor().getHost().getOwningKey();
        logger.info(" \uD83D\uDD34 Investor publicKey: ".concat(supplierPublicKey.toString()));
        if (!requiredSigners.contains(investorPublicKey)) {
            throw new IllegalArgumentException("Investor Party must sign");
        }
        PublicKey customerPublicKey = invoiceState.getCustomer().getHost().getOwningKey();
        logger.info(" \uD83D\uDD34 Customer publicKey: ".concat(supplierPublicKey.toString()));
        if (!requiredSigners.contains(customerPublicKey)) {
            throw new IllegalArgumentException("Customer Party must sign");
        }
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 InvoiceOfferContract: verification done OK! .....\uD83E\uDD1F \uD83E\uDD1F ");

    }

    public static class Register implements CommandData {}
    public static class MakeOffer implements CommandData {}
    public static class BuyOffer implements CommandData {}
}
