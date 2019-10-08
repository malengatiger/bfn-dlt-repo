package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceOfferContract;
import com.bfn.contracts.InvoiceTokenType;
import com.bfn.flows.admin.BFNCordaService;
import com.bfn.flows.regulator.ReportToRegulatorFlow;
import com.bfn.states.InvoiceOfferState;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@InitiatingFlow
@StartableByRPC
public class BuyInvoiceOfferFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(BuyInvoiceOfferFlow.class);

    private final StateAndRef<InvoiceOfferState> invoiceOfferState;
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
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... BuyInvoiceOfferFlow call started ...");
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);

        checkIfAlreadyConsumed(serviceHub);
        //todo - just a CordaService test ..no-op
        BFNCordaService bfnCordaService = serviceHub.cordaService(BFNCordaService.class);
        bfnCordaService.getInfo();

        InvoiceOfferContract.BuyOffer command = new InvoiceOfferContract.BuyOffer();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 InvoiceOfferContract.BuyOffer Notary: " + notary.getName().toString()
                + "  \uD83C\uDF4A supplierParty: " + invoiceOfferState.getState().getData().getSupplier().getName()
                + "  \uD83C\uDF4AinvestorParty: " + invoiceOfferState.getState().getData().getInvestor().getName()
                + " \uD83C\uDF4E  discount: " + invoiceOfferState.getState().getData().getDiscount()
                + "  \uD83D\uDC9A offerAmount" + invoiceOfferState.getState().getData().getOfferAmount());

        logger.info("\uD83D\uDC38 Ref State: txHash: ".concat(invoiceOfferState.getRef().getTxhash().toString()
                .concat(" \uD83D\uDC38 index: ")
                .concat("" + invoiceOfferState.getRef().getIndex())));
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        InvoiceOfferState oldState = invoiceOfferState.getState().getData();
        InvoiceOfferState offerState = new InvoiceOfferState(
                oldState.getInvoiceId(),
                oldState.getOfferAmount(),
                oldState.getDiscount(),
                oldState.getOriginalAmount(),
                oldState.getSupplier(),
                oldState.getInvestor(),
                oldState.getInvestor(),
                oldState.getOfferDate(),
                new Date(), oldState.getSupplierPublicKey(),
                oldState.getInvestorPublicKey(),
                oldState.getInvoiceNumber(),
                oldState.getCustomer());

        Party supplierParty = invoiceOfferState.getState().getData().getSupplier().getHost();
        Party investorParty = invoiceOfferState.getState().getData().getInvestor().getHost();
        Party customerParty = invoiceOfferState.getState().getData().getCustomer().getHost();

        PublicKey investorKey = investorParty.getOwningKey();
        PublicKey supplierKey = supplierParty.getOwningKey();
        PublicKey customerKey = customerParty.getOwningKey();

        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addInputState(invoiceOfferState);
        txBuilder.addOutputState(offerState, InvoiceOfferContract.ID);

        txBuilder.addCommand(command, supplierKey, investorKey, customerKey);

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        // Signing the transaction.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 BuyInvoiceOfferFlow signInitialTransaction executed ...");

        NodeInfo nodeInfo = serviceHub.getMyInfo();
        String investorOrg = offerState.getInvestor().getHost().getName().getOrganisation();
        String supplierOrg = offerState.getSupplier().getHost().getName().getOrganisation();
        String customerOrg = offerState.getCustomer().getHost().getName().getOrganisation();

        String thisNodeOrg = nodeInfo.getLegalIdentities().get(0).getName().getOrganisation();
        Matrix matrix = new Matrix();

        matrix.supplierIsRemote = !supplierOrg.equalsIgnoreCase(thisNodeOrg);
        matrix.investorIsRemote = !investorOrg.equalsIgnoreCase(thisNodeOrg);
        matrix.customerIsRemote = !customerOrg.equalsIgnoreCase(thisNodeOrg);

        if (!matrix.supplierIsRemote && !matrix.customerIsRemote && !matrix.investorIsRemote) {
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 All participants are LOCAL ...");
            SignedTransaction mSignedTransactionDone = subFlow(
                    new FinalityFlow(signedTx, ImmutableList.of(), FINALISING_TRANSACTION.childProgressTracker()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  SAME NODE ==> " +
                    "FinalityFlow has been executed ... \uD83E\uDD66 \uD83E\uDD66");
            reportToRegulator(serviceHub, mSignedTransactionDone);
            return mSignedTransactionDone;
        }

        FlowSession supplierSession;
        FlowSession investorSession;
        FlowSession customerSession;
        SignedTransaction signedTransaction;

        if (matrix.supplierIsRemote && matrix.customerIsRemote && matrix.investorIsRemote) {
            logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 All participants are REMOTE");
            investorSession = initiateFlow(investorParty);
            supplierSession = initiateFlow(supplierParty);
            customerSession = initiateFlow(customerParty);
            signedTransaction = getSignedTransaction(signedTx,
                    ImmutableList.of(investorSession, supplierSession, customerSession));
            return signedTransaction;
        }
        logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 Some participants are REMOTE and some are LOCAL");
        List<FlowSession> flowSessions = new ArrayList<>();
        if (matrix.customerIsRemote) {
            customerSession = initiateFlow(customerParty);
            flowSessions.add(customerSession);
        }
        if (matrix.supplierIsRemote) {
            supplierSession = initiateFlow(supplierParty);
            flowSessions.add(supplierSession);

        }
        if (matrix.investorIsRemote) {
            investorSession = initiateFlow(investorParty);
            flowSessions.add(investorSession);
        }
        logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 Number of Flow Sessions for REMOTE participants: "
                + flowSessions.size()
                + " - signing transactions on different nodes");
        signedTransaction = getSignedTransaction(signedTx, flowSessions);
        reportToRegulator(serviceHub,signedTransaction);
        return signedTransaction;

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


    @Suspendable
    private void checkIfAlreadyConsumed(ServiceHub serviceHub) throws FlowException {
        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(
                Vault.StateStatus.CONSUMED);
        Vault.Page page = serviceHub.getVaultService().queryBy(InvoiceOfferState.class, criteria,
                new PageSpecification(1, 200));
        List<StateAndRef<InvoiceOfferState>> refs = page.getStates();
        boolean isFound = false;
        logger.info(" \uD83D\uDCA6 \uD83D\uDCA6 Number of consumed InvoiceOfferStates: " +
                "\uD83D\uDCA6 " + refs.size() + "  \uD83D\uDCA6");

        for (StateAndRef<InvoiceOfferState> ref : refs) {
            InvoiceOfferState state = ref.getState().getData();
            if (invoiceOfferState.getState().getData().getInvoiceId().toString()
                    .equalsIgnoreCase(state.getInvoiceId().toString())) {
                isFound = true;
            }
        }
        if (isFound) {
            logger.warn(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 " +
                    "Attempt to consume state ALREADY consumed  \uD83D\uDE21  \uD83D\uDE21 ");
            throw new FlowException("InvoiceOfferState is already  \uD83D\uDE21 CONSUMED");
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
                " \uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C MULTIPLE NODE(S): FinalityFlow has been executed ... " +
                "\uD83E\uDD66 \uD83E\uDD66");
        return mSignedTransactionDone;
    }

    private class Matrix {
        boolean supplierIsRemote, customerIsRemote, investorIsRemote;
    }
}

