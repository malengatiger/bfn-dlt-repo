package com.bfn.flows.tokens;

import com.bfn.states.InvoiceOfferState;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;

@InitiatingFlow
@StartableByRPC
public class IssueTokensFlow extends FlowLogic<SignedTransaction> {

    final InvoiceOfferState invoiceOfferState;

    public IssueTokensFlow(InvoiceOfferState invoiceOfferState) {
        this.invoiceOfferState = invoiceOfferState;
    }

    @Override
    public SignedTransaction call() throws FlowException {
        return null;
    }
}
