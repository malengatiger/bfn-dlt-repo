package com.bfn.schemas;

import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.PersistentStateRef;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.security.PublicKey;
import java.util.Date;
import java.util.UUID;

/**
 * An InvoiceOfferSchema schema.
 */
//@CordaSerializable
public class InvoiceOfferSchemaV1  {
//    public InvoiceOfferSchemaV1() {
//        super(InvoiceOfferSchema.class, 1, ImmutableList.of(PersistentInvoiceOffer.class));
//    }
//
//    @Entity
//    @Table(name = "invoice_offer_states")
//    public static class PersistentInvoiceOffer extends PersistentState {
//        @Column(name = "invoiceId")
//        private final UUID invoiceId;
//        @Column(name = "offerAmount")
//        private final double offerAmount;
//        @Column(name = "discount")
//        private final double discount;
//        @Column(name = "supplier")
//        private final AccountInfo supplier;
//        @Column(name = "investor")
//        private final AccountInfo investor;
//        @Column(name = "owner")
//        private final AccountInfo owner;
//        @Column(name = "offerDate")
//        private final Date offerDate;
//        @Column(name = "ownerDate")
//        private final Date ownerDate;
//        @Column(name = "supplierPublicKey")
//        private final PublicKey supplierPublicKey;
//        @Column(name = "investorPublicKey")
//        private final PublicKey investorPublicKey;
//
//        public PersistentInvoiceOffer(@Nullable PersistentStateRef stateRef, UUID invoiceId, double offerAmount, double discount, AccountInfo supplier, AccountInfo investor, AccountInfo owner, Date offerDate, Date ownerDate, PublicKey supplierPublicKey, PublicKey investorPublicKey) {
//            super(stateRef);
//            this.invoiceId = invoiceId;
//            this.offerAmount = offerAmount;
//            this.discount = discount;
//            this.supplier = supplier;
//            this.investor = investor;
//            this.owner = owner;
//            this.offerDate = offerDate;
//            this.ownerDate = ownerDate;
//            this.supplierPublicKey = supplierPublicKey;
//            this.investorPublicKey = investorPublicKey;
//        }
//
//        public PersistentInvoiceOffer(UUID invoiceId, double offerAmount, double discount,
//                                      AccountInfo supplier, AccountInfo investor, AccountInfo owner,
//                                      Date offerDate, Date ownerDate, PublicKey supplierPublicKey, PublicKey investorPublicKey) {
//            this.invoiceId = invoiceId;
//            this.offerAmount = offerAmount;
//            this.discount = discount;
//            this.supplier = supplier;
//            this.investor = investor;
//            this.owner = owner;
//            this.offerDate = offerDate;
//            this.ownerDate = ownerDate;
//            this.supplierPublicKey = supplierPublicKey;
//            this.investorPublicKey = investorPublicKey;
//        }
//    }
}
