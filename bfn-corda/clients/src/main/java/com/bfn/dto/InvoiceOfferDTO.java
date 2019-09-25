package com.bfn.dto;

import java.util.Date;

public class InvoiceOfferDTO {
    private String invoiceId, invoiceNumber;
    private double offerAmount, discount, originalAmount;
    private  AccountInfoDTO supplier, investor, owner, customer;
    private  Date offerDate, investorDate;
    private String supplierPublicKey, investorPublicKey;

    public InvoiceOfferDTO(String invoiceId, String invoiceNumber,
                           double offerAmount, double discount,
                           double originalAmount,
                           AccountInfoDTO supplier, AccountInfoDTO investor,
                           AccountInfoDTO owner, Date offerDate, Date investorDate,
                           String supplierPublicKey, String investorPublicKey, AccountInfoDTO customer) {
        this.invoiceId = invoiceId;
        this.offerAmount = offerAmount;
        this.discount = discount;
        this.originalAmount = originalAmount;
        this.supplier = supplier;
        this.investor = investor;
        this.owner = owner;
        this.offerDate = offerDate;
        this.investorDate = investorDate;
        this.supplierPublicKey = supplierPublicKey;
        this.investorPublicKey = investorPublicKey;
        this.invoiceNumber = invoiceNumber;
        this.customer = customer;
    }

    public InvoiceOfferDTO() {
    }

    public AccountInfoDTO getCustomer() {
        return customer;
    }

    public void setCustomer(AccountInfoDTO customer) {
        this.customer = customer;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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

    public double getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
    }

    public AccountInfoDTO getSupplier() {
        return supplier;
    }

    public void setSupplier(AccountInfoDTO supplier) {
        this.supplier = supplier;
    }

    public AccountInfoDTO getInvestor() {
        return investor;
    }

    public void setInvestor(AccountInfoDTO investor) {
        this.investor = investor;
    }

    public AccountInfoDTO getOwner() {
        return owner;
    }

    public void setOwner(AccountInfoDTO owner) {
        this.owner = owner;
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
}
