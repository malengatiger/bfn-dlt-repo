package com.bfn.dto;

public class DashboardData {
    private int accounts, invoices, offers;
    private String node;

    public DashboardData(int accounts, int invoices, int offers, String node) {
        this.accounts = accounts;
        this.invoices = invoices;
        this.offers = offers;
        this.node = node;
    }

    public DashboardData() {
    }

    public int getAccounts() {
        return accounts;
    }

    public void setAccounts(int accounts) {
        this.accounts = accounts;
    }

    public int getInvoices() {
        return invoices;
    }

    public void setInvoices(int invoices) {
        this.invoices = invoices;
    }

    public int getOffers() {
        return offers;
    }

    public void setOffers(int offers) {
        this.offers = offers;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }
}
