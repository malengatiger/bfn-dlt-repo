package com.bfn.dto;

import java.util.Date;

public class InvoiceDTO {
    String invoiceId;
    String invoiceNumber;
    String description;

    Double amount, totalAmount, valueAddedTax;
    private Date dateRegistered;
    private AccountInfoDTO supplier, customer;
    private String supplierPublicKey, customerPublicKey;

    public InvoiceDTO() {
    }

    public InvoiceDTO(String invoiceId, String invoiceNumber,
                      String description, Double amount, Double totalAmount,
                      Double valueAddedTax, Date dateRegistered,
                      AccountInfoDTO supplier, AccountInfoDTO customer, String supplierPublicKey, String customerPublicKey) {
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.description = description;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.valueAddedTax = valueAddedTax;
        this.dateRegistered = dateRegistered;
        this.supplier = supplier;
        this.customer = customer;
        this.supplierPublicKey = supplierPublicKey;
        this.customerPublicKey = customerPublicKey;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getValueAddedTax() {
        return valueAddedTax;
    }

    public void setValueAddedTax(Double valueAddedTax) {
        this.valueAddedTax = valueAddedTax;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public AccountInfoDTO getSupplier() {
        return supplier;
    }

    public void setSupplier(AccountInfoDTO supplier) {
        this.supplier = supplier;
    }

    public AccountInfoDTO getCustomer() {
        return customer;
    }

    public void setCustomer(AccountInfoDTO customer) {
        this.customer = customer;
    }

    public String getSupplierPublicKey() {
        return supplierPublicKey;
    }

    public void setSupplierPublicKey(String supplierPublicKey) {
        this.supplierPublicKey = supplierPublicKey;
    }

    public String getCustomerPublicKey() {
        return customerPublicKey;
    }

    public void setCustomerPublicKey(String customerPublicKey) {
        this.customerPublicKey = customerPublicKey;
    }
}
