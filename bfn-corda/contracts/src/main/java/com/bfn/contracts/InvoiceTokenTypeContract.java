package com.bfn.contracts;

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceTokenTypeContract extends EvolvableTokenContract implements Contract {
    private final static Logger logger = LoggerFactory.getLogger(InvoiceTokenTypeContract.class);


    @Override
    public void additionalCreateChecks(@NotNull LedgerTransaction tx) {
        logger.info(" \uD83C\uDF4F  \uD83C\uDF4F additionalCreateChecks: ");

    }

    @Override
    public void additionalUpdateChecks(@NotNull LedgerTransaction tx) {
        logger.info(" \uD83C\uDF3A  \uD83C\uDF3A additionalUpdateChecks: ");
    }
}
