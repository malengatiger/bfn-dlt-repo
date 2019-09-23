package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceOfferContract;
import com.bfn.flows.admin.BFNCordaService;
import com.bfn.states.InvoiceOfferState;
import com.bfn.states.InvoiceState;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccountFlow;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
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

    private static final int LOCAL_SUPPLIER = 1, LOCAL_INVESTOR = 2, REMOTE_SUPPLIER = 3, REMOTE_INVESTOR = 4;

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... RegisterInvoiceFlow call started ...");
        logger.info("  invoiceOfferState: InvoiceId: ".concat(invoiceOfferState.getInvoiceId().toString()));
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);

        checkDuplicate(serviceHub);

        BFNCordaService bfnCordaService = serviceHub.cordaService(BFNCordaService.class);
        bfnCordaService.getInfo();

        InvoiceOfferContract.MakeOffer command = new InvoiceOfferContract.MakeOffer();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().toString()
                + "  \uD83C\uDF4A supplierParty: " + invoiceOfferState.getSupplier().getName()
                + "  \uD83C\uDF4AinvestorParty: " + invoiceOfferState.getInvestor().getName()
                + " \uD83C\uDF4E  discount: " + invoiceOfferState.getDiscount()
                + "  \uD83D\uDC9A offerAmount" + invoiceOfferState.getOfferAmount());

        Party supplierParty = invoiceOfferState.getSupplier().getHost();
        Party investorParty = invoiceOfferState.getInvestor().getHost();
        PublicKey investorKey = investorParty.getOwningKey();
        PublicKey supplierKey = supplierParty.getOwningKey();
//        PublicKey investorKey = subFlow(new RequestKeyForAccount(invoiceOfferState.getInvestor())).getOwningKey();
//        PublicKey supplierKey = subFlow(new RequestKeyForAccount(invoiceOfferState.getSupplier())).getOwningKey();


        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addOutputState(invoiceOfferState, InvoiceOfferContract.ID);
        txBuilder.addCommand(command, supplierKey,
                investorKey);

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Offer TransactionBuilder verified");
        // Signing the transaction.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Offer Transaction signInitialTransaction executed ...");

        NodeInfo nodeInfo = serviceHub.getMyInfo();
        String investorOrg = invoiceOfferState.getInvestor().getHost().getName().getOrganisation();
        String supplierOrg = invoiceOfferState.getSupplier().getHost().getName().getOrganisation();
        String thisNodeOrg = nodeInfo.getLegalIdentities().get(0).getName().getOrganisation();
        int supplierStatus, investorStatus;
        if (supplierOrg.equalsIgnoreCase(thisNodeOrg)) {
            supplierStatus = LOCAL_SUPPLIER;
        } else {
            supplierStatus = REMOTE_SUPPLIER;
        }
        if (investorOrg.equalsIgnoreCase(thisNodeOrg)) {
            investorStatus = LOCAL_INVESTOR;
        } else {
            investorStatus = REMOTE_INVESTOR;
        }
        if (supplierStatus == LOCAL_SUPPLIER && investorStatus == LOCAL_INVESTOR) {
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Supplier and Customer are on the same node ...");
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Registration: signInitialTransaction executed ...");
            SignedTransaction mSignedTransactionDone = subFlow(
                    new FinalityFlow(signedTx, ImmutableList.of(), FINALISING_TRANSACTION.childProgressTracker()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  SAME NODE ==> " +
                    "FinalityFlow has been executed ... \uD83E\uDD66 \uD83E\uDD66");
            return mSignedTransactionDone;
        }
        logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 Supplier and Customer are NOT on the same node ..." +
                "  \uD83D\uDE21 flowSession(s) required");

        FlowSession supplierSession;
        FlowSession investorSession;
        SignedTransaction signedTransaction = null;
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Registration: signInitialTransaction executed ...");

        if (supplierStatus == LOCAL_SUPPLIER && investorStatus == REMOTE_INVESTOR) {
            logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 LOCAL_SUPPLIER and REMOTE_INVESTOR ...");
            investorSession = initiateFlow(investorParty);
            signedTransaction = getSignedTransaction(signedTx, ImmutableList.of(investorSession));
        }
        if (supplierStatus == REMOTE_SUPPLIER && investorStatus == LOCAL_INVESTOR) {
            logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 REMOTE_SUPPLIER and LOCAL_INVESTOR ...");
            supplierSession = initiateFlow(supplierParty);
            signedTransaction = getSignedTransaction(signedTx, ImmutableList.of(supplierSession));
        }
        if (supplierStatus == REMOTE_SUPPLIER && investorStatus == REMOTE_INVESTOR) {
            logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 REMOTE_SUPPLIER and REMOTE_INVESTOR ...");
            supplierSession = initiateFlow(supplierParty);
            investorSession = initiateFlow(investorParty);
            signedTransaction = getSignedTransaction(signedTx, ImmutableList.of(supplierSession, investorSession));
        }

        return signedTransaction;

    }
    @Suspendable
    private void checkDuplicate(ServiceHub serviceHub) throws FlowException {
        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page page = serviceHub.getVaultService().queryBy(InvoiceOfferState.class, criteria,
                new PageSpecification(1, 200));
        List<StateAndRef<InvoiceOfferState>> refs = page.getStates();
        boolean isFound = false;
        logger.info(" \uD83D\uDCA6  \uD83D\uDCA6 Number of InvoiceOfferStates:  \uD83D\uDCA6 " + refs.size() + "  \uD83D\uDCA6");
        for (StateAndRef<InvoiceOfferState> ref : refs) {
            InvoiceOfferState state = ref.getState().getData();
            if (invoiceOfferState.getInvoiceId().toString()
                    .equalsIgnoreCase(state.getInvoiceId().toString())
                    && invoiceOfferState.getInvestor().getIdentifier().getId().toString()
                    .equalsIgnoreCase(state.getInvestor().getIdentifier().getId().toString())) {
                isFound = true;
            }
        }
        if (isFound) {
            throw new FlowException("InvoiceOfferState is already on file");
        }
    }

    @Suspendable
    private SignedTransaction getSignedTransaction(SignedTransaction signedTx, List<FlowSession> sessions)
            throws FlowException {
        logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 getSignedTransaction ... sessions: " + sessions.size());
        progressTracker.setCurrentStep(GATHERING_SIGNATURES);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 ... Collecting Signatures ....");
        SignedTransaction signedTransaction = subFlow(
                new CollectSignaturesFlow(signedTx,
                        sessions,
                        GATHERING_SIGNATURES.childProgressTracker()));
        logger.info(("\uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD  Signatures collected OK!  \uD83D\uDE21 \uD83D\uDE21 " +
                ".... will call FinalityFlow ... \uD83C\uDF3A \uD83C\uDF3A txId: ")
                .concat(signedTransaction.getId().toString()));

        SignedTransaction mSignedTransactionDone = subFlow(
                new FinalityFlow(signedTransaction, sessions,
                        FINALISING_TRANSACTION.childProgressTracker()));
        logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  " +
                " \uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C OTHER NODE(S): FinalityFlow has been executed ... " +
                "\uD83E\uDD66 \uD83E\uDD66");
        return mSignedTransactionDone;
    }
}
