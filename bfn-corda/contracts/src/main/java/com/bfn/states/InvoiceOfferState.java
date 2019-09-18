package com.bfn.states;

import com.bfn.contracts.InvoiceContract;
import com.bfn.contracts.InvoiceOfferContract;
import com.bfn.schemas.InvoiceOfferSchemaV1;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@CordaSerializable
@BelongsToContract(InvoiceOfferContract.class)
public class InvoiceOfferState implements ContractState {
    private final static Logger logger = LoggerFactory.getLogger(InvoiceContract.class);
    private final UUID invoiceId;
    private final double offerAmount, discount;
    private final AccountInfo supplier, investor, owner;
    private final Date offerDate, ownerDate;
    private final PublicKey supplierPublicKey, investorPublicKey;

    public InvoiceOfferState(UUID invoiceId, double offerAmount, double discount, AccountInfo supplier, AccountInfo investor, AccountInfo owner, Date offerDate, Date ownerDate, PublicKey supplierPublicKey, PublicKey investorPublicKey) {
        this.invoiceId = invoiceId;
        this.offerAmount = offerAmount;
        this.discount = discount;
        this.supplier = supplier;
        this.investor = investor;
        this.owner = owner;
        this.offerDate = offerDate;
        this.ownerDate = ownerDate;
        this.supplierPublicKey = supplierPublicKey;
        this.investorPublicKey = investorPublicKey;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(supplier.getHost(),
                investor.getHost());
    }

    public PublicKey getSupplierPublicKey() {
        return supplierPublicKey;
    }

    public PublicKey getInvestorPublicKey() {
        return investorPublicKey;
    }

    public AccountInfo getOwner() {
        return owner;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public double getOfferAmount() {
        return offerAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public AccountInfo getSupplier() {
        return supplier;
    }

    public AccountInfo getInvestor() {
        return investor;
    }

    public Date getOfferDate() {
        return offerDate;
    }

    public Date getOwnerDate() {
        return ownerDate;
    }
//
//    @NotNull
//    @Override
//    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
//        if (schema instanceof InvoiceOfferSchemaV1) {
//            logger.info("\uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 PersistentState generateMappedObject returning new object: \uD83D\uDCA6 PersistentInvoiceOffer");
//            return new InvoiceOfferSchemaV1.PersistentInvoiceOffer(
//                    this.invoiceId,this.offerAmount,
//                    this.discount, this.supplier,this.investor, this.owner,
//                    this.offerDate,this.ownerDate,this.supplierPublicKey,
//                    this.investorPublicKey);
//        } else {
//            throw new IllegalArgumentException("Object fucked");
//        }
//    }
//
//    @NotNull
//    @Override
//    public Iterable<MappedSchema> supportedSchemas() {
//        return ImmutableList.of(new InvoiceOfferSchemaV1());
//    }
}
