package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceOfferContract;
import com.bfn.states.InvoiceOfferState;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.KeyManagementService;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class BuyInvoiceOfferFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(BuyInvoiceOfferFlow.class);

    final StateAndRef<InvoiceOfferState> invoiceOfferState;
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

    public BuyInvoiceOfferFlow(StateAndRef<InvoiceOfferState> invoiceOfferState) {
        this.invoiceOfferState = invoiceOfferState;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... BuyInvoiceOfferFlow call started ...");
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
        Vault.Page page = serviceHub.getVaultService().queryBy(InvoiceOfferState.class,criteria,
                new PageSpecification(1,200));
        List<StateAndRef<InvoiceOfferState>> refs = page.getStates();
        boolean isFound = false;
        logger.info(" \uD83D\uDCA6  \uD83D\uDCA6 Number of consumed InvoiceOfferStates:  \uD83D\uDCA6 " + refs.size() + "  \uD83D\uDCA6");
        for (StateAndRef<InvoiceOfferState> ref: refs) {
            InvoiceOfferState state = ref.getState().getData();
            if (invoiceOfferState.getState().getData().getInvoiceId().toString()
                    .equalsIgnoreCase(state.getInvoiceId().toString())) {
                isFound = true;
            }
        }
        if (isFound) {
            throw new FlowException("InvoiceOfferState is already CONSUMED");
        }

        InvoiceOfferContract.BuyOffer command = new InvoiceOfferContract.BuyOffer();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().toString()
                + "  \uD83C\uDF4A supplierParty: " + invoiceOfferState.getState().getData().getSupplier().getName()
                + "  \uD83C\uDF4AinvestorParty: "+ invoiceOfferState.getState().getData().getInvestor().getName()
                +" \uD83C\uDF4E  discount: " + invoiceOfferState.getState().getData().getDiscount()
                + "  \uD83D\uDC9A offerAmount" + invoiceOfferState.getState().getData().getOfferAmount());

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        InvoiceOfferState oldState = invoiceOfferState.getState().getData();
        InvoiceOfferState offerState = new InvoiceOfferState(
                oldState.getInvoiceId(),
                oldState.getOfferAmount(),
                oldState.getDiscount(),
                oldState.getSupplier(),
                oldState.getInvestor(),
                oldState.getInvestor(),
                oldState.getOfferDate(),
                new Date(), oldState.getSupplierPublicKey(), oldState.getInvestorPublicKey());

        Party supplierParty = invoiceOfferState.getState().getData().getSupplier().getHost();
        Party investorParty = invoiceOfferState.getState().getData().getInvestor().getHost();;

        PublicKey investorKey = investorParty.getOwningKey();
        PublicKey supplierKey = supplierParty.getOwningKey();

        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addInputState(invoiceOfferState);
        txBuilder.addOutputState(offerState, InvoiceOfferContract.ID);

        txBuilder.addCommand(command, supplierKey, investorKey);

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        // Signing the transaction.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 BuyInvoiceOfferFlow signInitialTransaction executed ...");

        NodeInfo nodeInfo = serviceHub.getMyInfo();
        String investorOrg = offerState.getInvestor().getHost().getName().getOrganisation();
        String supplierOrg = offerState.getSupplier().getHost().getName().getOrganisation();

        if (investorOrg.equalsIgnoreCase(supplierOrg)) {
            logger.info(" \uD83C\uDFC0  \uD83C\uDFC0  \uD83C\uDFC0 Supplier and Investor are on the same node. FlowSession not required");
            SignedTransaction mSignedTransactionDone = subFlow(
                    new FinalityFlow(signedTx, ImmutableList.of(),
                            FINALISING_TRANSACTION.childProgressTracker()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  " +
                    "FinalityFlow has been executed ... \uD83E\uDD66 \uD83E\uDD66");
            return mSignedTransactionDone;
        } else {
            logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21  Supplier and Investor are NOT on the same node." +
                    "  \uD83D\uDE21 FlowSession is required");
            FlowSession otherPartyFlowSession = null;
            String thisNodeOrg = nodeInfo.getLegalIdentities().get(0).getName().getOrganisation();

            if (supplierOrg.equalsIgnoreCase(thisNodeOrg)) {
                otherPartyFlowSession = initiateFlow(investorParty);
                logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 ... FlowSession set up for  \uD83D\uDE21 investor: "
                        .concat(offerState.getInvestor().getName()));
            }
            if (investorOrg.equalsIgnoreCase(thisNodeOrg)) {
                otherPartyFlowSession = initiateFlow(supplierParty);
                logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 ... FlowSession set up for  \uD83D\uDE21 supplier: "
                        .concat(offerState.getSupplier().getName()));
            }
            if (otherPartyFlowSession == null) {
                throw new IllegalStateException("Unable to set up FlowSession: investor: "
                        .concat(investorOrg).concat(" supplier: ".concat(supplierOrg)));
            }
            progressTracker.setCurrentStep(GATHERING_SIGNATURES);
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Collecting Signatures ....");
            SignedTransaction signedTransaction = subFlow(
                    new CollectSignaturesFlow(signedTx,
                            ImmutableList.of(otherPartyFlowSession),
                            GATHERING_SIGNATURES.childProgressTracker()));
            logger.info(("\uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD Signatures collected OK!  \uD83D\uDE21 \uD83D\uDE21 " +
                    "will call FinalityFlow ... \uD83C\uDF3A \uD83C\uDF3A  \uD83C\uDF3A \uD83C\uDF3A : ")
                    .concat(signedTransaction.getId().toString()));

            SignedTransaction mSignedTransactionDone = subFlow(
                    new FinalityFlow(signedTransaction, ImmutableList.of(otherPartyFlowSession),
                            FINALISING_TRANSACTION.childProgressTracker()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D " +
                    " FinalityFlow has been executed ... \uD83E\uDD66 \uD83E\uDD66");
            return mSignedTransactionDone;
        }
    }
}
