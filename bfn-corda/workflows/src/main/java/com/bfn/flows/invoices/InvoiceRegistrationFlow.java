package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceContract;
import com.bfn.states.InvoiceState;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Date;
import java.util.Objects;

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
        logger.info("\uD83C\uDF3A \uD83C\uDF3A RegisterInvoiceFlow constructor with invoiceState: \uD83C\uDF4F "
                + invoiceState.getSupplierInfo().getName());
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... RegisterInvoiceFlow call started ...");
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        invoiceState.setDateRegistered(new Date());

        Party supplierParty = invoiceState.getSupplierInfo().getHost();
        Party customerParty = invoiceState.getCustomerInfo().getHost();

        PublicKey customerKey = supplierParty.getOwningKey();
        PublicKey supplierKey = customerParty.getOwningKey();

        String customerOrg = invoiceState.getCustomerInfo().getHost().getName().getOrganisation();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 customerParty key: ".concat(customerKey.toString()));
        String supplierOrg = invoiceState.getSupplierInfo().getHost().getName().getOrganisation();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 supplierParty key: ".concat(supplierKey.toString()));

        InvoiceState msState = new InvoiceState(invoiceState.getInvoiceId(),invoiceState.getInvoiceNumber(),
                invoiceState.getDescription(),invoiceState.getAmount(),invoiceState.getTotalAmount(),
                invoiceState.getValueAddedTax(),invoiceState.getSupplierInfo(),
                invoiceState.getCustomerInfo(),supplierKey,customerKey);


        InvoiceContract.Register command = new InvoiceContract.Register();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().getOrganisation()
                + "  \uD83C\uDF4A supplierInfo: " + invoiceState.getSupplierInfo().getName()
                + "  \uD83C\uDF4A customerInfo: " + invoiceState.getCustomerInfo().getName()+ " \uD83C\uDF4E  invoice: "
                + invoiceState.getInvoiceNumber().concat("  \uD83D\uDC9A totalAmount") + invoiceState.getTotalAmount());

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(msState, InvoiceContract.ID)
                .addCommand(command, supplierKey, customerKey);

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Register TransactionBuilder verified");
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Registration: signInitialTransaction executed ...");

        NodeInfo nodeInfo = serviceHub.getMyInfo();
        String thisNodeOrg = nodeInfo.getLegalIdentities().get(0).getName().getOrganisation();

        if (supplierOrg.equalsIgnoreCase(customerOrg)) {
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Supplier and Customer are on the same node ...");
            SignedTransaction mSignedTransactionDone = subFlow(
                    new FinalityFlow(signedTx, ImmutableList.of(), FINALISING_TRANSACTION.childProgressTracker()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  SAME NODE ==> FinalityFlow has been executed ... \uD83E\uDD66 \uD83E\uDD66");
            return mSignedTransactionDone;
        } else {
            logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 Supplier and Customer are NOT on the same node ..." +
                    "  \uD83D\uDE21 flowSession required");
            FlowSession otherPartyFlowSession = null;
            if (supplierOrg.equalsIgnoreCase(thisNodeOrg)) {
                otherPartyFlowSession = initiateFlow((Party) customerParty);
            }
            if (otherPartyFlowSession != null) {
                logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 ... FlowSession set up for customer: "
                        .concat(invoiceState.getCustomerInfo().getName()));
            }
            if (customerOrg.equalsIgnoreCase(thisNodeOrg)) {
                otherPartyFlowSession = initiateFlow((Party) supplierParty);
                logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 ... FlowSession set up for supplier: "
                        .concat(invoiceState.getSupplierInfo().getName()));
            }
            if (otherPartyFlowSession == null) {
                throw new IllegalStateException("Unable to set up FlowSession: customer: "
                        .concat(customerOrg).concat(" supplier: ".concat(supplierOrg)));
            } else {
                logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 ... FlowSession set up OK");
            }

            progressTracker.setCurrentStep(GATHERING_SIGNATURES);
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 ... Collecting Signatures ....");
            SignedTransaction signedTransaction = subFlow(
                    new CollectSignaturesFlow(signedTx,
                            ImmutableList.of(otherPartyFlowSession),
                            GATHERING_SIGNATURES.childProgressTracker()));
            logger.info(("\uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD  Signatures collected OK!  \uD83D\uDE21 \uD83D\uDE21 " +
                    ".... will call FinalityFlow ... \uD83C\uDF3A \uD83C\uDF3A txId: ")
                    .concat(signedTransaction.getId().toString()));

            SignedTransaction mSignedTransactionDone = subFlow(
                    new FinalityFlow(signedTx, ImmutableList.of(otherPartyFlowSession),
                            FINALISING_TRANSACTION.childProgressTracker()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  OTHER NODE: FinalityFlow has been executed ... \uD83E\uDD66 \uD83E\uDD66");
            return mSignedTransactionDone;
        }
    }
}
