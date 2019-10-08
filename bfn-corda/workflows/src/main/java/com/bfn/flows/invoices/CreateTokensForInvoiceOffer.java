package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceTokenType;
import com.bfn.flows.regulator.ReportToRegulatorFlow;
import com.bfn.states.InvoiceOfferState;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

@StartableByRPC
public class CreateTokensForInvoiceOffer extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(CreateTokensForInvoiceOffer.class);


   private final InvoiceOfferState invoiceOfferState;
   private final InvoiceTokenType invoiceTokenType;


    public CreateTokensForInvoiceOffer(InvoiceOfferState invoiceOfferState, InvoiceTokenType invoiceTokenType) {
        this.invoiceOfferState = invoiceOfferState;
        this.invoiceTokenType = invoiceTokenType;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... CreateTokenTypeFlow call started ...");

        if (!invoiceOfferState.getInvestor().getIdentifier().getId().toString()
                .equalsIgnoreCase(invoiceOfferState.getOwner().getIdentifier().getId().toString())) {
            throw new FlowException("Offer investor and owner must be the same");
        }
        TokenPointer tokenPointer = invoiceTokenType.toPointer(invoiceTokenType.getClass());
        IssuedTokenType issuedTokenType = new IssuedTokenType(getOurIdentity(), tokenPointer);
        Amount<IssuedTokenType> amount = new Amount<>(invoiceOfferState.getOfferAmount().longValue(),
                issuedTokenType);


        FungibleToken fungibleToken = new FungibleToken(amount,
                invoiceOfferState.getInvestor().getHost(),null);
        logger.info(" \uD83D\uDC4C\uD83C\uDFFE  \uD83D\uDC4C\uD83C\uDFFE FungibleToken created: ".concat(fungibleToken.toString()));

        TransactionState transactionState = new TransactionState<>(fungibleToken,
                getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0));
        SignedTransaction tx = subFlow(new CreateEvolvableTokens(transactionState));

        logger.info("\uD83D\uDD90\uD83C\uDFFD \uD83D\uDD90\uD83C\uDFFD \uD83D\uDD90\uD83C\uDFFD " +
                "InvoiceOffer Tokens created \uD83E\uDD6C ");
        reportToRegulator(getServiceHub(),tx);
        return tx;
    }
    @Suspendable
    private void reportToRegulator(ServiceHub serviceHub, SignedTransaction mSignedTransactionDone) throws FlowException {
        logger.info("\uD83D\uDCCC \uD83D\uDCCC \uD83D\uDCCC  Talking to the Regulator, for compliance, Senor! .............");
        Set<Party> parties = serviceHub.getIdentityService().partiesFromName("Regulator",false);
        Party regulator = parties.iterator().next();
        try {
            subFlow(new ReportToRegulatorFlow(regulator,mSignedTransactionDone));
            logger.info("\uD83D\uDCCC \uD83D\uDCCC \uD83D\uDCCC  DONE talking to the Regulator, Phew!");

        } catch (Exception e) {
            logger.error(" \uD83D\uDC7F  \uD83D\uDC7F  \uD83D\uDC7F Regulator fell down.  \uD83D\uDC7F IGNORED  \uD83D\uDC7F ", e);
            throw new FlowException("Regulator fell down!");
        }
    }


}

