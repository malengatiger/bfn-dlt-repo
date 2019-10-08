package com.bfn.dto;

import com.bfn.states.InvoiceOfferState;
import net.corda.core.identity.Party;

import java.security.PublicKey;

public class InvoiceTokenTypeDTO {
    private  Party investor;
    private  Party customer, supplier;
    private  PublicKey investorKey, customerKey, supplierKey, holderKey;
    private  InvoiceOfferState invoiceOfferState;
}
