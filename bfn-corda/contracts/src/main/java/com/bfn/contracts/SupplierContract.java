package com.bfn.contracts;

import com.bfn.states.SupplierState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

// ************
// * Contract *
// ************
@SuppressWarnings("unchecked")
public class SupplierContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.bfn.contracts.SupplierContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException{

        if (tx.getInputStates().size() != 0) {
            throw new IllegalArgumentException("Input states must be zero");
        }
        if (tx.getOutputStates().size() != 1) {
            throw new IllegalArgumentException("One output SupplierState is required");
        }
        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Only one command allowed");
        }
        Command command = tx.getCommand(0);
        if (!(command.getValue() instanceof Register)) {
            throw new IllegalArgumentException("Only Register command allowed");
        }
        List<PublicKey> requiredSigners = command.getSigners();

        ContractState contractState = tx.getOutput(0);
        if (!(contractState instanceof  SupplierState)) {
            throw new IllegalArgumentException("Output state must be SupplierState");
        }
        SupplierState supplierState = (SupplierState)contractState;
        if (supplierState.getName() == null) {
            throw new IllegalArgumentException("Supplier name is required");
        }
        Party party = supplierState.getParty();
        PublicKey key = party.getOwningKey();
        if (!requiredSigners.contains(key)) {
            throw new IllegalArgumentException("Supplier Party must sign");
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Action implements Commands {}
    }

    public static class Register implements CommandData {}
}
