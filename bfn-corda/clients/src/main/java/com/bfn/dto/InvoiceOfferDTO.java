package com.bfn.dto;

import com.r3.corda.lib.accounts.contracts.states.AccountInfo;

import java.util.Date;
import java.util.UUID;

public class InvoiceOfferDTO {
     String invoiceId;
     double offerAmount, discount;
     String supplierId, investorId, ownerId;
     Date offerDate, investorDate;

    public InvoiceOfferDTO(String invoiceId, double offerAmount, double discount,
                           String supplierId, String investorId, String ownerId) {
        this.invoiceId = invoiceId;
        this.offerAmount = offerAmount;
        this.discount = discount;
        this.supplierId = supplierId;
        this.investorId = investorId;
        this.ownerId = ownerId;
    }

    public InvoiceOfferDTO() {
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public double getOfferAmount() {
        return offerAmount;
    }

    public void setOfferAmount(double offerAmount) {
        this.offerAmount = offerAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getInvestorId() {
        return investorId;
    }

    public void setInvestorId(String investorId) {
        this.investorId = investorId;
    }

    public Date getOfferDate() {
        return offerDate;
    }

    public void setOfferDate(Date offerDate) {
        this.offerDate = offerDate;
    }

    public Date getInvestorDate() {
        return investorDate;
    }

    public void setInvestorDate(Date investorDate) {
        this.investorDate = investorDate;
    }
}
