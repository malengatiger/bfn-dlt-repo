package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceContract;
import com.bfn.flows.admin.BFNCordaService;
import com.bfn.states.InvoiceOfferState;
import com.bfn.states.InvoiceState;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
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
import java.util.Date;
import java.util.List;
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

    private static final int LOCAL_SUPPLIER = 1, LOCAL_CUSTOMER = 2, REMOTE_SUPPLIER = 3, REMOTE_CUSTOMER = 4;

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... RegisterInvoiceFlow call started ...");
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        invoiceState.setDateRegistered(new Date());

        BFNCordaService bfnCordaService = serviceHub.cordaService(BFNCordaService.class);
        bfnCordaService.getInfo();

        checkDuplicate(serviceHub);

        Party supplierParty = invoiceState.getSupplierInfo().getHost();
        Party customerParty = invoiceState.getCustomerInfo().getHost();

        PublicKey customerKey = supplierParty.getOwningKey();
        PublicKey supplierKey = customerParty.getOwningKey();

        String customerOrg = invoiceState.getCustomerInfo().getHost().getName().getOrganisation();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 customerParty key: ".concat(customerKey.toString()));
        String supplierOrg = invoiceState.getSupplierInfo().getHost().getName().getOrganisation();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 supplierParty key: ".concat(supplierKey.toString()));

        InvoiceState msState = new InvoiceState(invoiceState.getInvoiceId(), invoiceState.getInvoiceNumber(),
                invoiceState.getDescription(), invoiceState.getAmount(), invoiceState.getTotalAmount(),
                invoiceState.getValueAddedTax(), invoiceState.getSupplierInfo(),
                invoiceState.getCustomerInfo(), supplierKey, customerKey);


        InvoiceContract.Register command = new InvoiceContract.Register();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().getOrganisation()
                + "  \uD83C\uDF4A supplierInfo: " + invoiceState.getSupplierInfo().getName()
                + "  \uD83C\uDF4A customerInfo: " + invoiceState.getCustomerInfo().getName() + " \uD83C\uDF4E  invoice: "
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

        NodeInfo nodeInfo = serviceHub.getMyInfo();
        String thisNodeOrg = nodeInfo.getLegalIdentities().get(0).getName().getOrganisation();
        int supplierStatus, customerStatus;
        if (supplierOrg.equalsIgnoreCase(thisNodeOrg)) {
            supplierStatus = LOCAL_SUPPLIER;
        } else {
            supplierStatus = REMOTE_SUPPLIER;
        }
        if (customerOrg.equalsIgnoreCase(thisNodeOrg)) {
            customerStatus = LOCAL_CUSTOMER;
        } else {
            customerStatus = REMOTE_CUSTOMER;
        }

        if (supplierStatus == LOCAL_SUPPLIER && customerStatus == LOCAL_CUSTOMER) {
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Supplier and Customer are on the same node ...");
            logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Registration: signInitialTransaction executed ...");
            SignedTransaction mSignedTransactionDone = subFlow(
                    new FinalityFlow(signedTx, ImmutableList.of(), FINALISING_TRANSACTION.childProgressTracker()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D  SAME NODE ==> FinalityFlow has been executed ... \uD83E\uDD66 \uD83E\uDD66");
            return mSignedTransactionDone;
        }
        logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 Supplier and Customer are NOT on the same node ..." +
                "  \uD83D\uDE21 flowSession(s) required");

        FlowSession supplierSession;
        FlowSession customerSession;
        SignedTransaction signedTransaction = null;
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Registration: signInitialTransaction executed ...");

        if (supplierStatus == LOCAL_SUPPLIER && customerStatus == REMOTE_CUSTOMER) {
            logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 LOCAL_SUPPLIER and REMOTE_CUSTOMER ...");
            customerSession = initiateFlow(customerParty);
            signedTransaction = getSignedTransaction(signedTx, ImmutableList.of(customerSession));
        }
        if (supplierStatus == REMOTE_SUPPLIER && customerStatus == LOCAL_CUSTOMER) {
            logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 REMOTE_SUPPLIER and LOCAL_CUSTOMER ...");
            supplierSession = initiateFlow(supplierParty);
            signedTransaction = getSignedTransaction(signedTx, ImmutableList.of(supplierSession));
        }
        if (supplierStatus == REMOTE_SUPPLIER && customerStatus == REMOTE_CUSTOMER) {
            logger.info(" \uD83D\uDE21  \uD83D\uDE21  \uD83D\uDE21 REMOTE_SUPPLIER and REMOTE_CUSTOMER ...");
            supplierSession = initiateFlow(supplierParty);
            customerSession = initiateFlow(customerParty);
            signedTransaction = getSignedTransaction(signedTx, ImmutableList.of(supplierSession, customerSession));
        }

        return signedTransaction;

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
    @Suspendable
    private void checkDuplicate(ServiceHub serviceHub) throws FlowException {
        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page page = serviceHub.getVaultService().queryBy(InvoiceState.class, criteria,
                new PageSpecification(1, 200));
        List<StateAndRef<InvoiceState>> refs = page.getStates();
        boolean isFound = false;
        logger.info(" \uD83D\uDCA6  \uD83D\uDCA6 Number of InvoiceStates:  \uD83D\uDCA6 " + refs.size() + "  \uD83D\uDCA6");
        for (StateAndRef<InvoiceState> ref : refs) {
            InvoiceState state = ref.getState().getData();
            if (invoiceState.getInvoiceNumber().toString()
                    .equalsIgnoreCase(state.getInvoiceNumber())
                    && invoiceState.getSupplierInfo().getIdentifier().getId().toString()
                    .equalsIgnoreCase(state.getSupplierInfo().getIdentifier().getId().toString())) {
                isFound = true;
            }
        }
        if (isFound) {
            throw new FlowException("InvoiceState is already on file");
        }
    }
}
