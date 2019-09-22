package com.bfn.states;

import com.bfn.contracts.InvoiceContract;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

// *********
// * State *
// *********
@BelongsToContract(InvoiceContract.class)
@CordaSerializable
public class InvoiceState implements ContractState {

    private final UUID invoiceId;
    private final String invoiceNumber;
    private final String description;
    private final Double amount, totalAmount, valueAddedTax;
    private Date dateRegistered;
    private final AccountInfo supplierInfo, customerInfo;
    private final PublicKey supplierPublicKey, customerPublicKey;
    private final static Logger logger = LoggerFactory.getLogger(InvoiceState.class);

    public InvoiceState(UUID invoiceId, String invoiceNumber, String description, Double amount, Double totalAmount,
                        Double valueAddedTax, AccountInfo supplierInfo, AccountInfo customerInfo,
                        PublicKey supplierPublicKey, PublicKey customerPublicKey, Date dateRegistered) {
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.description = description;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.valueAddedTax = valueAddedTax;
        this.supplierInfo = supplierInfo;
        this.customerInfo = customerInfo;
        this.supplierPublicKey = supplierPublicKey;
        this.customerPublicKey = customerPublicKey;
        this.dateRegistered = dateRegistered;
        if (dateRegistered == null) {
            this.dateRegistered = new Date();
        }
    }



    public UUID getInvoiceId() {
        return invoiceId;
    }

    public AccountInfo getCustomerInfo() {
        return customerInfo;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getDescription() {
        return description;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public Double getValueAddedTax() {
        return valueAddedTax;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public AccountInfo getSupplierInfo() {
        return supplierInfo;
    }

    public PublicKey getSupplierPublicKey() {
        return supplierPublicKey;
    }

    public PublicKey getCustomerPublicKey() {
        return customerPublicKey;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(supplierInfo.getHost(),
                customerInfo.getHost());
    }


}
