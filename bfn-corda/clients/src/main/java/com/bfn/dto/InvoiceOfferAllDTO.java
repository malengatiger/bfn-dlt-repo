package com.bfn.dto;

public class InvoiceOfferAllDTO {
    private String invoiceId;
    private double offerAmount, discount;
    private  String accountId;

    public InvoiceOfferAllDTO(String invoiceId, double offerAmount, double discount, String accountId) {
        this.invoiceId = invoiceId;
        this.offerAmount = offerAmount;
        this.discount = discount;
        this.accountId = accountId;
    }

    public InvoiceOfferAllDTO() {
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
