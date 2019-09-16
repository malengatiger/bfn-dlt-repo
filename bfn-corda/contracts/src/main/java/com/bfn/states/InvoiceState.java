package com.bfn.states;

import com.bfn.contracts.InvoiceContract;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.serialization.CordaSerializable;

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

    private UUID invoiceId;
    private String invoiceNumber;
    private String description;
    private Double amount, totalAmount, valueAddedTax;
    private Date dateRegistered;
    private AccountInfo supplierInfo, customerInfo;

    public InvoiceState(
            String invoiceNumber, String description,
            Double amount, Double totalAmount, Double valueAddedTax,
            Date dateRegistered, AccountInfo supplierInfo, AccountInfo customerInfo, UUID invoiceId) {
        this.invoiceId = invoiceId;
        this.customerInfo = customerInfo;
        this.invoiceNumber = invoiceNumber;
        this.description = description;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.valueAddedTax = valueAddedTax;
        this.dateRegistered = dateRegistered;
        this.supplierInfo = supplierInfo;
        if (dateRegistered == null) {
            this.dateRegistered = new Date();
        }
        if (invoiceId == null) {
            this.invoiceId = UUID.randomUUID();
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

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    @Override
    public List<AbstractParty> getParticipants() {

        return Arrays.asList(supplierInfo.getHost(), customerInfo.getHost());
    }


}
