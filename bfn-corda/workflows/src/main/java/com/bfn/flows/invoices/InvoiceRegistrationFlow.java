package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceContract;
import com.bfn.states.InvoiceState;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@InitiatingFlow
@StartableByRPC
public class InvoiceRegistrationFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(InvoiceRegistrationFlow.class);

    final InvoiceState invoiceState;
    private final ProgressTracker.Step SENDING_TRANSACTION = new ProgressTracker.Step("Sending transaction to counterParty");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new IOU.");
    private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step GATHERING_SIGNATURES = new ProgressTracker.Step("Gathering the counterparty's signature.") {
        @Override
        public ProgressTracker childProgressTracker() {
            logger.info("\uD83C\uDF3A \uD83C\uDF3A ProgressTracker childProgressTracker ...");
            return CollectSignaturesFlow.Companion.tracker();
        }
    };
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.Companion.tracker();
        }
    };

    // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
    // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
    // function.
    private final ProgressTracker progressTracker = new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGNATURES,
            FINALISING_TRANSACTION,
            SENDING_TRANSACTION
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    public InvoiceRegistrationFlow(InvoiceState invoiceState) {
        this.invoiceState = invoiceState;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A RegisterInvoiceFlow constructor with invoiceState: \uD83C\uDF4F " + invoiceState.getSupplierInfo().getName().toString());
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... RegisterInvoiceFlow call started ...");
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        invoiceState.setDateRegistered(new Date());

        Party customerParty = invoiceState.getCustomerInfo().getHost();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 customerParty: ".concat(customerParty.toString()));

        Party supplierParty = invoiceState.getSupplierInfo().getHost();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 supplierParty: ".concat(supplierParty.toString()));

        InvoiceContract.Register command = new InvoiceContract.Register();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().toString()
                + "  \uD83C\uDF4A supplierInfo: " + invoiceState.getSupplierInfo().toString()
                + "  \uD83C\uDF4A customerInfo: " + invoiceState.getCustomerInfo().toString() + " \uD83C\uDF4E  invoice: "
                + invoiceState.getInvoiceNumber().concat("  \uD83D\uDC9A totalAmount") + invoiceState.getTotalAmount());

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(invoiceState, InvoiceContract.ID)
                .addCommand(command, supplierParty.getOwningKey(), customerParty.getOwningKey());

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Register TransactionBuilder verified");
        // Signing the transaction.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Register Transaction signInitialTransaction executed ...");
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Transaction signInitialTransaction: ".concat(signedTx.toString()));

        if (supplierParty.getOwningKey().toString().equalsIgnoreCase(customerParty.getOwningKey().toString())) {
            SignedTransaction mSignedTransactionDone = subFlow(new FinalityFlow(signedTx, ImmutableList.of(), FINALISING_TRANSACTION.childProgressTracker()));
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 FinalityFlow has been executed ... \uD83E\uDD66  are we good? \uD83E\uDD66");
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 returning mSignedTransactionDone:: ".concat(mSignedTransactionDone.toString()));
            return mSignedTransactionDone;
        } else {
            FlowSession customerFlowSession = initiateFlow(customerParty);
            progressTracker.setCurrentStep(GATHERING_SIGNATURES);
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Collecting Signatures ....");
            SignedTransaction signedTransaction = subFlow(
                    new CollectSignaturesFlow(signedTx,
                            ImmutableList.of(customerFlowSession),
                            GATHERING_SIGNATURES.childProgressTracker()));
            logger.info(("\uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD  Signatures collected OK!  \uD83D\uDE21 \uD83D\uDE21 " +
                    ".... will call FinalityFlow ... \uD83C\uDF3A \uD83C\uDF3A  \uD83C\uDF3A \uD83C\uDF3A : ").concat(signedTransaction.toString()));

            SignedTransaction mSignedTransactionDone = subFlow(new FinalityFlow(signedTx, ImmutableList.of(), FINALISING_TRANSACTION.childProgressTracker()));
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 FinalityFlow has been executed ... \uD83E\uDD66  are we good? \uD83E\uDD66");
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 returning mSignedTransactionDone:: ".concat(mSignedTransactionDone.toString()));
            return mSignedTransactionDone;
        }
    }
}
