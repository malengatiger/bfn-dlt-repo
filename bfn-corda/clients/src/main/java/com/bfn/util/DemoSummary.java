package com.bfn.util;

public class DemoSummary {
    int numberOfAccounts, numberOfInvoices, numberOfInvoiceOffers;
    int numberOfNodes, numberOfFlows;
    String started, ended;
    double elapsedSeconds;

    public DemoSummary() {
    }

    public DemoSummary(int numberOfAccounts, int numberOfInvoices, int numberOfInvoiceOffers, int numberOfNodes, int numberOfFlows, String started, String ended, double elapsedSeconds) {
        this.numberOfAccounts = numberOfAccounts;
        this.numberOfInvoices = numberOfInvoices;
        this.numberOfInvoiceOffers = numberOfInvoiceOffers;
        this.numberOfNodes = numberOfNodes;
        this.numberOfFlows = numberOfFlows;
        this.started = started;
        this.ended = ended;
        this.elapsedSeconds = elapsedSeconds;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(double elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getEnded() {
        return ended;
    }

    public void setEnded(String ended) {
        this.ended = ended;
    }

    public int getNumberOfAccounts() {
        return numberOfAccounts;
    }

    public void setNumberOfAccounts(int numberOfAccounts) {
        this.numberOfAccounts = numberOfAccounts;
    }

    public int getNumberOfInvoices() {
        return numberOfInvoices;
    }

    public void setNumberOfInvoices(int numberOfInvoices) {
        this.numberOfInvoices = numberOfInvoices;
    }

    public int getNumberOfInvoiceOffers() {
        return numberOfInvoiceOffers;
    }

    public void setNumberOfInvoiceOffers(int numberOfInvoiceOffers) {
        this.numberOfInvoiceOffers = numberOfInvoiceOffers;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public int getNumberOfFlows() {
        return numberOfFlows;
    }

    public void setNumberOfFlows(int numberOfFlows) {
        this.numberOfFlows = numberOfFlows;
    }
}
