package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceOfferContract;
import com.bfn.states.InvoiceOfferState;
import com.bfn.states.InvoiceState;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@InitiatingFlow
@StartableByRPC
public class InvoiceOfferFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(InvoiceOfferFlow.class);

    final InvoiceOfferState invoiceOfferState;
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

    public InvoiceOfferFlow(InvoiceOfferState invoiceOfferState) {
        this.invoiceOfferState = invoiceOfferState;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A InvoiceOfferFlow constructor with invoiceOfferState supplier: \uD83C\uDF4F "
                + invoiceOfferState.getSupplier().toString() + "\n investor: ".concat(invoiceOfferState.getInvestor().toString()));

    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... RegisterInvoiceFlow call started ...");
        logger.info("  invoiceOfferState: InvoiceId: ".concat(invoiceOfferState.getInvoiceId().toString()));
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);

//        //todo - figure out vaultQuery criteria search
//        List<StateAndRef<InvoiceState>> stateAndRefs = serviceHub.getVaultService().queryBy(InvoiceState.class).getStates();
//        logger.info("\uD83D\uDC99 \uD83D\uDC99 \uD83D\uDC99 \uD83D\uDC99 stateAndRefsFound: " +  stateAndRefs.size());
//        StateAndRef<InvoiceState> invoiceStateStateAndRef = null;
//        for (StateAndRef<InvoiceState> ref: stateAndRefs) {
//            String invoiceId = ref.getState().getData().getInvoiceId().toString();
//            String invoiceId2 = invoiceOfferState.getInvoiceId().toString();
//                    logger.info("\uD83D\uDC99 compare: ".concat(invoiceId).concat("\n").concat(invoiceId2));
//            if (invoiceId.equalsIgnoreCase(invoiceId2)) {
//                invoiceStateStateAndRef = ref;
//                logger.info("\uD83E\uDD66 \uD83E\uDD66 \uD83E\uDD66 stateAndRef for invoice found.");
//            }
//        }
//
//        if (invoiceStateStateAndRef == null) {
//            throw new IllegalArgumentException("Unable to obtain invoice stateAndRef");
//        }
        InvoiceOfferContract.MakeOffer command = new InvoiceOfferContract.MakeOffer();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().toString()
                + "  \uD83C\uDF4A supplierParty: " + invoiceOfferState.getSupplier().getName()
                + "  \uD83C\uDF4AinvestorParty: "+ invoiceOfferState.getInvestor().getName()
                +" \uD83C\uDF4E  discount: " + invoiceOfferState.getDiscount()
                + "  \uD83D\uDC9A offerAmount" + invoiceOfferState.getOfferAmount());

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addOutputState(invoiceOfferState, InvoiceOfferContract.ID);
        txBuilder.addCommand(command, invoiceOfferState.getSupplier().getHost().getOwningKey(),
                invoiceOfferState.getInvestor().getHost().getOwningKey());

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Offer TransactionBuilder verified");
        // Signing the transaction.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Offer Transaction signInitialTransaction executed ...");
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Transaction signInitialTransaction: ".concat(signedTx.toString()));

        if (invoiceOfferState.getInvestor().getHost().getName().toString()
                .equalsIgnoreCase(invoiceOfferState.getSupplier().getHost().getName().toString())) {
            logger.info(" \uD83C\uDFC0  \uD83C\uDFC0  \uD83C\uDFC0 Supplier and Investor are on the same node. FlowSession not required");
            SignedTransaction mSignedTransactionDone = subFlow(new FinalityFlow(signedTx, ImmutableList.of(), FINALISING_TRANSACTION.childProgressTracker()));
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 FinalityFlow has been executed ... \uD83E\uDD66  are we good? \uD83E\uDD66");
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 returning mSignedTransactionDone:: ".concat(mSignedTransactionDone.toString()));
            return mSignedTransactionDone;
        } else {
            logger.info(" \uD83C\uDFC0  \uD83C\uDFC0  \uD83C\uDFC0 Supplier and Investor are NOT on the same node. FlowSession is required");
            FlowSession investorFlowSession = initiateFlow(invoiceOfferState.getInvestor().getHost());
            progressTracker.setCurrentStep(GATHERING_SIGNATURES);
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Collecting Signatures ....");
            SignedTransaction signedTransaction = subFlow(
                    new CollectSignaturesFlow(signedTx,
                            ImmutableList.of(investorFlowSession),
                            GATHERING_SIGNATURES.childProgressTracker()));
            logger.info(("\uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD  Signatures collected OK!  \uD83D\uDE21 \uD83D\uDE21 " +
                    "will call FinalityFlow ... \uD83C\uDF3A \uD83C\uDF3A  \uD83C\uDF3A \uD83C\uDF3A : ").concat(signedTransaction.toString()));

            SignedTransaction mSignedTransactionDone = subFlow(new FinalityFlow(signedTransaction, ImmutableList.of(investorFlowSession), FINALISING_TRANSACTION.childProgressTracker()));
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 FinalityFlow has been executed ... \uD83E\uDD66  are we good? \uD83E\uDD66");
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 returning mSignedTransactionDone:: ".concat(mSignedTransactionDone.toString()));
            return mSignedTransactionDone;
        }
    }
}
