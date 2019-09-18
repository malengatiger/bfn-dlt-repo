package com.bfn.contracts;

import com.bfn.states.InvoiceState;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

// ************
// * Contract *
// ************

public class InvoiceContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = InvoiceContract.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(InvoiceContract.class);

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException{

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 InvoiceContract: verify starting ..... \uD83E\uDD6C \uD83E\uDD6C ");
        if (tx.getInputStates().size() != 0) {
            throw new IllegalArgumentException("Input states must be zero");
        }
        if (tx.getOutputStates().size() != 1) {
            throw new IllegalArgumentException("One output InvoiceState is required");
        }
        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Only one command allowed");
        }
        Command command = tx.getCommand(0);
        if (!(command.getValue() instanceof Register)) {
            throw new IllegalArgumentException("Only Register command allowed");
        }
        List<PublicKey> requiredSigners = command.getSigners();
        logger.info(" \uD83D\uDD34  \uD83D\uDD34 Required signers: " + requiredSigners.size());
        for (PublicKey key: requiredSigners) {
            logger.info(" \uD83D\uDD34 Required signer: publicKey: ".concat(key.toString()));
        }
        ContractState contractState = tx.getOutput(0);
        if (!(contractState instanceof InvoiceState)) {
            throw new IllegalArgumentException("Output state must be InvoiceState");
        }
        InvoiceState invoiceState = (InvoiceState)contractState;
        if (invoiceState.getSupplierInfo() == null) {
            throw new IllegalArgumentException("Supplier is required");
        }
        if (invoiceState.getCustomerInfo() == null) {
            throw new IllegalArgumentException("Customer is required");
        }

        if (!requiredSigners.contains(invoiceState.getSupplierInfo().getHost().getOwningKey())) {
            throw new IllegalArgumentException("Supplier Party must sign");
        }

        if (!requiredSigners.contains(invoiceState.getCustomerInfo().getHost().getOwningKey())) {
            throw new IllegalArgumentException("Customer Party must sign");
        }
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 InvoiceContract: verification done OK! .....\uD83E\uDD1F \uD83E\uDD1F ");

    }

    public static class Register implements CommandData {}
}
