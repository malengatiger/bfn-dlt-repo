package com.bfn.dto;

import com.r3.corda.lib.accounts.contracts.states.AccountInfo;

import java.util.Date;
import java.util.UUID;

public class InvoiceOfferDTO {
     String invoiceId;
     double offerAmount, discount, originalAmount;
     String supplierId, investorId, ownerId;
     Date offerDate, investorDate;
    private String supplierPublicKey, investorPublicKey;

    public InvoiceOfferDTO(String invoiceId, double offerAmount, double discount, double originalAmount, String supplierId, String investorId, String ownerId, Date offerDate, Date investorDate, String supplierPublicKey, String investorPublicKey) {
        this.invoiceId = invoiceId;
        this.offerAmount = offerAmount;
        this.discount = discount;
        this.originalAmount = originalAmount;
        this.supplierId = supplierId;
        this.investorId = investorId;
        this.ownerId = ownerId;
        this.offerDate = offerDate;
        this.investorDate = investorDate;
        this.supplierPublicKey = supplierPublicKey;
        this.investorPublicKey = investorPublicKey;
    }

    public InvoiceOfferDTO() {
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getSupplierPublicKey() {
        return supplierPublicKey;
    }

    public void setSupplierPublicKey(String supplierPublicKey) {
        this.supplierPublicKey = supplierPublicKey;
    }

    public String getInvestorPublicKey() {
        return investorPublicKey;
    }

    public void setInvestorPublicKey(String investorPublicKey) {
        this.investorPublicKey = investorPublicKey;
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
