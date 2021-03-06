package com.bfn.util;

import com.bfn.contracts.InvoiceTokenType;
import com.bfn.dto.*;
import com.bfn.flows.admin.AccountRegistrationFlow;
import com.bfn.flows.admin.ShareAccountInfoFlow;
import com.bfn.flows.invoices.*;
import com.bfn.states.InvoiceOfferState;
import com.bfn.states.InvoiceState;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.node.services.vault.Sort;
import net.corda.core.transactions.SignedTransaction;
import org.bouncycastle.jcajce.provider.symmetric.DSTU7624;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class WorkerBee {
    private final static Logger logger = LoggerFactory.getLogger(WorkerBee.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final Firestore db = FirestoreClient.getFirestore();

    public static List<NodeInfoDTO> listNodes(CordaRPCOps proxy) {

        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        List<NodeInfoDTO> nodeList = new ArrayList();
        for (NodeInfo info : nodes) {
            NodeInfoDTO dto = new NodeInfoDTO();
            dto.setSerial(info.getSerial());
            dto.setPlatformVersion(info.getPlatformVersion());
            for (Party party : info.getLegalIdentities()) {
                dto.setAddresses(new ArrayList());
                dto.getAddresses().add(party.getName().toString());
            }

            logger.info("\uD83C\uDF3A \uD83C\uDF3A BFN Corda Node: \uD83C\uDF3A "
                    + info.getLegalIdentities().get(0).getName().toString());
            nodeList.add(dto);
        }
        logger.info(" \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A Corda NetworkNodes found: \uD83D\uDC9A "
                + nodeList.size() + " \uD83D\uDC9A ");
        return nodeList;
    }

    public static List<NodeInfoDTO> listFirestoreNodes() throws ExecutionException, InterruptedException {
        List<NodeInfoDTO> nodeList = new ArrayList();
        ApiFuture<QuerySnapshot> future = db.collection("nodes").get();
        QuerySnapshot snapshots = future.get();
        List<QueryDocumentSnapshot> list = snapshots.getDocuments();
        for (QueryDocumentSnapshot snapshot : list) {
            Map<String, Object> map = snapshot.getData();
            NodeInfoDTO node = new NodeInfoDTO();
            node.setWebAPIUrl((String) map.get("webAPIUrl"));
            node.setSerial((Long) map.get("serial"));
            node.setPlatformVersion((Long) map.get("platformVersion"));
            node.setAddresses(new ArrayList());
            node.getAddresses().add(String.valueOf(map.get("addresses")));
            nodeList.add(node);
        }

        return nodeList;
    }

    public static List<NodeInfoDTO> writeNodesToFirestore(CordaRPCOps proxy, Environment env) throws Exception {

        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        List<NodeInfoDTO> nodeList = new ArrayList<>();
        proxy.startFlowDynamic(com.r3.corda.lib.tokens.workflows.flows.issue.IssueTokensFlow.class);
        FirebaseUtil.deleteCollection("nodes");
        for (NodeInfo info : nodes) {
            NodeInfoDTO dto = new NodeInfoDTO();
            dto.setSerial(info.getSerial());
            dto.setPlatformVersion(info.getPlatformVersion());
            for (Party party : info.getLegalIdentities()) {
                dto.setAddresses(new ArrayList());
                dto.getAddresses().add(party.getName().toString());
            }
            switch (info.getLegalIdentities().get(0).getName().getOrganisation()) {
                case "OCTMainOffice":
                    String octURL = env.getProperty("OCT");
                    dto.setWebAPIUrl(octURL);
                    break;
                case "OCTCapeTown":
                    String ctURL = env.getProperty("CapeTown");
                    dto.setWebAPIUrl(ctURL);
                    break;
                case "OCTLondon":
                    String lonURL = env.getProperty("London");
                    dto.setWebAPIUrl(lonURL);
                    break;
                case "OCTNewYork":
                    String nyURL = env.getProperty("NewYork");
                    dto.setWebAPIUrl(nyURL);
                    break;
                case "Regulator":
                    String regURL = env.getProperty("Regulator");
                    dto.setWebAPIUrl(regURL);
                    break;
            }
            ApiFuture<DocumentReference> future = db.collection("nodes").add(dto);
            nodeList.add(dto);
            logger.info("\uD83C\uDF3A \uD83C\uDF3A Node written to Firestore: \uD83C\uDF3A "
                    + info.getLegalIdentities().get(0).getName().getOrganisation()
                    .concat(" -  \uD83D\uDD06 path: ".concat(future.get().getPath())));
        }
        if (nodeList.isEmpty()) {
            throw new Exception("Nodes not found");
        }
        logger.info(" \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A Corda NetworkNodes written: \uD83D\uDC9A "
                + nodeList.size() + " \uD83D\uDC9A ");
        return nodeList;
    }


    public static List<AccountInfoDTO> getAccounts(CordaRPCOps proxy) {

        List<StateAndRef<AccountInfo>> accounts = proxy.vaultQuery(AccountInfo.class).getStates();
        int cnt = 0;
        List<AccountInfoDTO> list = new ArrayList<>();
        for (StateAndRef<AccountInfo> ref : accounts) {
            cnt++;
//            logger.info(" \uD83C\uDF3A AccountInfo: #".concat("" + cnt + " :: ").concat(ref.getState().getData().toString()
//                    .concat(" \uD83E\uDD4F ")));
            AccountInfo info = ref.getState().getData();
            AccountInfoDTO dto = new AccountInfoDTO(info.getIdentifier().getId().toString(),
                    info.getHost().toString(), info.getName(), info.getStatus().name());
            list.add(dto);
        }
        String msg = "\uD83C\uDF3A  \uD83C\uDF3A done listing accounts:  \uD83C\uDF3A " + list.size();
        logger.info(msg);
        return list;
    }

    public static AccountInfoDTO getAccount(CordaRPCOps proxy, String accountId) throws Exception {

        List<AccountInfoDTO> list = getAccounts(proxy);
        AccountInfoDTO dto = null;
        for (AccountInfoDTO info : list) {
            if (info.getIdentifier().equalsIgnoreCase(accountId)) {
                dto = info;
                break;
            }
        }
        if (dto == null) {
            logger.warn("Account not found on BFN account");
            throw new Exception("Account not found on BFN network");
        }
        String msg = "\uD83C\uDF3A  \uD83C\uDF3A found account:  \uD83C\uDF3A " + GSON.toJson(dto);
        logger.info(msg);
        return dto;
    }

    public static List<InvoiceDTO> getInvoiceStates(CordaRPCOps proxy,
                                                    String accountId,
                                                    boolean consumed) throws Exception {

        logger.info("........................ accountId:  \uD83D\uDC9A ".concat(accountId == null ? "null" : accountId)
                .concat(" consumed:  \uD83D\uDC9A " + consumed));
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(
                consumed ? Vault.StateStatus.CONSUMED : Vault.StateStatus.UNCONSUMED);
        Vault.Page<InvoiceState> page = proxy.vaultQueryByWithPagingSpec(
                InvoiceState.class, criteria,
                new PageSpecification(1, 200));

        List<InvoiceDTO> list = new ArrayList<>();
        logger.info("\uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 Total invoices found: " + page.getStates().size());
        int cnt = 0;
        for (StateAndRef<InvoiceState> ref : page.getStates()) {
            InvoiceState m = ref.getState().getData();
            InvoiceDTO invoice = getDTO(m);
            cnt++;
//            logger.info("\uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 Invoice #"
//                    +cnt+" from stateAndRef, before check: " + GSON.toJson(invoice));

            if (accountId == null) {
                list.add(invoice);
//                logger.warn("........... accountId is null ... list: " + list.size());
            } else {

                if (invoice.getSupplier().getIdentifier().equalsIgnoreCase(accountId)
                        || invoice.getCustomer().getIdentifier().equalsIgnoreCase(accountId)) {
                    list.add(invoice);
//                    logger.warn("........... accountId is ".concat(accountId)
//                    .concat(" list: " + list.size()));
                }
            }

        }
        String m = " \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A  done listing InvoiceStates:  \uD83C\uDF3A " + list.size();
        logger.info(m);
        return list;
    }

    public static List<InvoiceOfferDTO> getInvoiceOfferStates(CordaRPCOps proxy, String accountId, boolean consumed) throws Exception {
        logger.info("...................... accountId:  \uD83D\uDC9A ".concat(accountId == null ? "null" : accountId)
                .concat(" consumed:  \uD83D\uDC9A " + consumed));
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(
                consumed ? Vault.StateStatus.CONSUMED : Vault.StateStatus.ALL);
        Vault.Page<InvoiceOfferState> page = proxy.vaultQueryByWithPagingSpec(
                InvoiceOfferState.class, criteria,
                new PageSpecification(1, 200));
        List<InvoiceOfferDTO> list = new ArrayList<>();
        logger.info("\uD83D\uDCA6 \uD83D\uDCA6 \uD83D\uDCA6 Total offers found: " + page.getStates().size());
        int cnt = 0;
        for (StateAndRef<InvoiceOfferState> ref : page.getStates()) {
            InvoiceOfferState offerState = ref.getState().getData();
            cnt++;
            InvoiceOfferDTO offer = getDTO(offerState);
            if (accountId == null) {
                list.add(offer);
            } else {
                if (offer.getSupplier().getIdentifier().equalsIgnoreCase(accountId)
                        || offer.getInvestor().getIdentifier().equalsIgnoreCase(accountId)
                        || offer.getCustomer().getIdentifier().equalsIgnoreCase(accountId)) {
                    list.add(offer);
                }
            }

        }
        String m = "\uD83D\uDCA6  done listing InvoiceOfferStates:  \uD83C\uDF3A " + list.size();
        logger.info(m);
        return list;
    }

    //todo extend paing query where appropriate
    private static final int PAGE_SIZE = 200;

    public static DashboardData getDashboardData(CordaRPCOps proxy) {
        int pageNumber = 1;
        List<StateAndRef<ContractState>> states = new ArrayList<>();
        DashboardData data = new DashboardData();
        long totalResults;
        do {
            logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 " +
                    "processing page " + pageNumber);
            PageSpecification pageSpec = new PageSpecification(pageNumber, PAGE_SIZE);
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<ContractState> results = proxy.vaultQueryByWithPagingSpec(
                    ContractState.class, criteria, pageSpec);
            totalResults = results.getTotalStatesAvailable();
            List<StateAndRef<ContractState>> newStates = results.getStates();
            logger.info("\uD83D\uDCA6 \uD83D\uDCA6 Number of States \uD83C\uDF4E " + newStates.size());
            states.addAll(results.getStates());
            pageNumber++;
        } while ((PAGE_SIZE * (pageNumber - 1) <= totalResults));

        int accts = 0, invoices = 0, offers = 0;
        int acctsp = 0, invoicesp = 0, offersp = 0;

        List<String> mList = new ArrayList<>();
        for (StateAndRef<ContractState> ref : states) {
            ContractState state = ref.getState().getData();
            String m = "\uD83E\uDDE9 \uD83E\uDDE9 " +
                    "State class: ".concat(state.getClass().getName())
                            .concat(" participants: " + state.getParticipants().size());
            if (m.contains("AccountInfo")) {
                accts++;
                acctsp = state.getParticipants().size();
            }
            if (m.contains("InvoiceState")) {
                invoices++;
                invoicesp = state.getParticipants().size();
            }
            if (m.contains("InvoiceOfferState ")) {
                offers++;
                offersp = state.getParticipants().size();
            }
        }
        NodeInfo info = proxy.nodeInfo();
        data.setNode(info.getLegalIdentities().get(0).getName().toString());
        data.setAccounts(accts);
        data.setInvoices(invoices);
        data.setOffers(offers);

        String t1 = "\n\n\uD83E\uDDE9 \uD83E\uDDE9 List of States on ".concat(info.getLegalIdentities().get(0).getName().toString()
                .concat(" \uD83E\uDDE9 \uD83E\uDDE9 "));
        String a1 = "\uD83E\uDDE9 \uD83E\uDDE9 AccountInfo found on node: \uD83C\uDF4E " + accts + " \uD83C\uDF4E partcipants:  \uD83E\uDDE1 " + acctsp;
        String a2 = "\uD83E\uDDE9 \uD83E\uDDE9 InvoiceStates found on node: \uD83C\uDF4E " + invoices + " \uD83C\uDF4E  partcipants:  \uD83E\uDDE1 " + invoicesp;
        String a3 = "\uD83E\uDDE9 \uD83E\uDDE9 InvoiceOfferStates found on node: \uD83C\uDF4E " + offers + " \uD83C\uDF4E  partcipants:  \uD83E\uDDE1 " + offersp;
        mList.add(t1);
        mList.add(a1);
        mList.add(a2);
        mList.add(a3);
        mList.add("\uD83E\uDDE9 \uD83E\uDDE9 Total states found:  \uD83E\uDDE1 " + (accts + invoices + offers) + "  \uD83E\uDDE1 \n\n");
        for (String m : mList) {
            logger.info(m);
        }
        return data;

    }

    public static List<String> getStates(CordaRPCOps proxy) {
        int pageNumber = 1;
        List<StateAndRef<ContractState>> states = new ArrayList<>();
        long totalResults;
        do {
            logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 " +
                    "processing page " + pageNumber);
            PageSpecification pageSpec = new PageSpecification(pageNumber, PAGE_SIZE);
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<ContractState> results = proxy.vaultQueryByWithPagingSpec(
                    ContractState.class, criteria, pageSpec);
            totalResults = results.getTotalStatesAvailable();
            List<StateAndRef<ContractState>> newStates = results.getStates();
            logger.info("\uD83D\uDCA6 \uD83D\uDCA6 Number of States \uD83C\uDF4E " + newStates.size());
            states.addAll(results.getStates());
            pageNumber++;
        } while ((PAGE_SIZE * (pageNumber - 1) <= totalResults));

        int accts = 0, invoices = 0, offers = 0;
        int acctsp = 0, invoicesp = 0, offersp = 0;

        List<String> mList = new ArrayList<>();
        for (StateAndRef<ContractState> ref : states) {
            ContractState state = ref.getState().getData();
            String m = "\uD83E\uDDE9 \uD83E\uDDE9 " +
                    "State class: ".concat(state.getClass().getName())
                            .concat(" participants: " + state.getParticipants().size());
            if (m.contains("AccountInfo")) {
                accts++;
                acctsp = state.getParticipants().size();
            }
            if (m.contains("InvoiceState")) {
                invoices++;
                invoicesp = state.getParticipants().size();
            }
            if (m.contains("InvoiceOfferState ")) {
                offers++;
                offersp = state.getParticipants().size();
            }
        }
        NodeInfo info = proxy.nodeInfo();
        String t1 = "\n\n\uD83E\uDDE9 \uD83E\uDDE9 List of States on ".concat(info.getLegalIdentities().get(0).getName().toString()
                .concat(" \uD83E\uDDE9 \uD83E\uDDE9 "));
        String a1 = "\uD83E\uDDE9 \uD83E\uDDE9 AccountInfo found on node: \uD83C\uDF4E " + accts + " \uD83C\uDF4E partcipants:  \uD83E\uDDE1 " + acctsp;
        String a2 = "\uD83E\uDDE9 \uD83E\uDDE9 InvoiceStates found on node: \uD83C\uDF4E " + invoices + " \uD83C\uDF4E  partcipants:  \uD83E\uDDE1 " + invoicesp;
        String a3 = "\uD83E\uDDE9 \uD83E\uDDE9 InvoiceOfferStates found on node: \uD83C\uDF4E " + offers + " \uD83C\uDF4E  partcipants:  \uD83E\uDDE1 " + offersp;
        mList.add(t1);
        mList.add(a1);
        mList.add(a2);
        mList.add(a3);
        mList.add("\uD83E\uDDE9 \uD83E\uDDE9 Total states found:  \uD83E\uDDE1 " + (accts + invoices + offers) + "  \uD83E\uDDE1 \n\n");
        for (String m : mList) {
            logger.info(m);
        }
        return mList;

    }

    public static List<String> listFlows(CordaRPCOps proxy) {

        logger.info("🥬 🥬 🥬 🥬 Registered Flows on Corda BFN ...  \uD83E\uDD6C ");
        List<String> flows = proxy.registeredFlows();
        int cnt = 0;
        for (String info : flows) {
            cnt++;
            logger.info("\uD83E\uDD4F \uD83E\uDD4F #$" + cnt + " \uD83E\uDD6C BFN Corda Flow:  \uD83E\uDD4F" + info + "   \uD83C\uDF4E ");
        }

        logger.info("🥬 🥬 🥬 🥬 Total Registered Flows  \uD83C\uDF4E  " + cnt + "  \uD83C\uDF4E \uD83E\uDD6C ");
        return flows;
    }

    public static List<String> listNotaries(CordaRPCOps proxy) {

        List<Party> notaryIdentities = proxy.notaryIdentities();
        List<String> list = new ArrayList<>();
        for (Party info : notaryIdentities) {
            logger.info(" \uD83D\uDD35  \uD83D\uDD35 BFN Corda Notary: \uD83C\uDF3A " + info.getName().toString());
            list.add(info.getName().toString());
        }
        return list;
    }

    public static InvoiceDTO startInvoiceRegistrationFlow(CordaRPCOps proxy, InvoiceDTO invoice) throws Exception {

//        logger.info("Input Parameters; \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F InvoiceDTO: "
//                + GSON.toJson(invoice) + " \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F");
        try {
//            logger.info("\uD83C\uDF4F SUPPLIER: ".concat(invoice.getSupplier().getName()).concat("  \uD83D\uDD06  ")
//                    .concat("  \uD83E\uDDE1 CUSTOMER: ").concat(invoice.getCustomer().getName()));

            List<StateAndRef<AccountInfo>> accounts = proxy.vaultQuery(AccountInfo.class).getStates();
            AccountInfo supplierInfo = null, customerInfo = null;
            for (StateAndRef<AccountInfo> info : accounts) {

                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getCustomer().getIdentifier())) {
                    customerInfo = info.getState().getData();
                }
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getSupplier().getIdentifier())) {
                    supplierInfo = info.getState().getData();
                }
            }
            if (supplierInfo == null) {
                throw new Exception("Supplier is fucking missing");
            }
            if (customerInfo == null) {
                throw new Exception("Customer is bloody missing");
            }
            double discAmt = invoice.getAmount() * (invoice.getValueAddedTax() / 100);
            double tot = invoice.getAmount() + discAmt;

            CordaFuture<AnonymousParty> anonymousPartyCordaFuture = proxy.startTrackedFlowDynamic(
                    RequestKeyForAccount.class, customerInfo).getReturnValue();
            PublicKey customerKey = anonymousPartyCordaFuture.get().getOwningKey();

            CordaFuture<AnonymousParty> anonymousPartyCordaFuture1 = proxy.startTrackedFlowDynamic(
                    RequestKeyForAccount.class, supplierInfo).getReturnValue();
            PublicKey supplierKey = anonymousPartyCordaFuture1.get().getOwningKey();

            invoice.setTotalAmount(tot);
            InvoiceState invoiceState = new InvoiceState(
                    UUID.randomUUID(), invoice.getInvoiceNumber(),
                    invoice.getDescription(),
                    new BigDecimal(invoice.getAmount()),
                    new BigDecimal(invoice.getTotalAmount()),
                    new BigDecimal(invoice.getValueAddedTax()),
                    supplierInfo,
                    customerInfo,
                    supplierKey,
                    customerKey,
                    new Date());
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    InvoiceRegistrationFlow.class, invoiceState).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  " +
                    "\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F "
                    + issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            InvoiceDTO dto = getDTO(invoiceState);
            try {
                FirebaseUtil.sendInvoiceMessage(dto);
                ApiFuture<DocumentReference> reference = db.collection("invoices").add(dto);
                logger.info(("\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 " +
                        "Firestore path: ").concat(reference.get().getPath()));

            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return dto;
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw new Exception("Failed to register invoice. ".concat(e.getMessage()));
            } else {
                throw new Exception("Failed to register invoice. Unknown cause");
            }
        }
    }

    public static InvoiceOfferDTO startBuyInvoiceOfferFlow(CordaRPCOps proxy, String invoiceId) throws Exception {

        try {

            //is the investor on their own node ???
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<InvoiceOfferState> page = proxy.vaultQueryByWithPagingSpec(
                    InvoiceOfferState.class, criteria,
                    new PageSpecification(1, 200));
            List<StateAndRef<InvoiceOfferState>> refs = page.getStates();
            StateAndRef<InvoiceOfferState> refToBuy = null;
            for (StateAndRef<InvoiceOfferState> ref : refs) {
                InvoiceOfferState state = ref.getState().getData();
                if (state.getInvoiceId().toString().equalsIgnoreCase(invoiceId)) {
                    refToBuy = ref;
                    break;
                }
            }
            if (refToBuy == null) {
                logger.error("\uD83D\uDC7F \uD83D\uDC7F \uD83D\uDC7F BuyInvoice failed. offer not found");
                throw new Exception("InvoiceOffer to buy not found");
            }
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    BuyInvoiceOfferFlow.class, refToBuy).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  " +
                    "\n\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F "
                    + issueTx.getId().toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            logger.info(" \uD83D\uDC9A \uD83D\uDC9A \uD83D\uDC9A Bought invoiceOffer:  \uD83C\uDF3A id: ".concat(refToBuy.getState().getData().getInvoiceNumber())
                    .concat("  \uD83C\uDF3A amount: " + refToBuy.getState().getData().getOfferAmount()));
            //create tokens
            try {
                Vault.Page page2 = proxy.vaultQuery(InvoiceTokenType.class);
                if (page2.getStates().isEmpty()) {
                    logger.error("\uD83D\uDC7F \uD83D\uDC7F InvoiceTokenType does not exist");
                } else {
                    InvoiceTokenType tokenType = (InvoiceTokenType) page2.getStates().get(0);

                    CordaFuture<SignedTransaction> signedTransactionCordaFuture2 =
                            proxy.startTrackedFlowDynamic(
                                    CreateTokensForInvoiceOffer.class, refToBuy.getState(),
                                    tokenType).getReturnValue();
                    SignedTransaction issueTx2 = signedTransactionCordaFuture2.get();
                    logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                            "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  " +
                            "\n\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F "
                            + issueTx2.getId().toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
                    logger.info(" \uD83D\uDC9A \uD83D\uDC9A \uD83D\uDC9A Invoice tokens :  \uD83C\uDF3A id: ".concat(tokenType.getLinearId().getId().toString())
                            .concat("  \uD83C\uDF3A amount: " + refToBuy.getState().getData().getOfferAmount()));
                }
            } catch (Exception e) {
                logger.error(e.getMessage() == null ? "\uD83D\uDC7F \uD83D\uDC7F Unable to create tokens for invoice: "
                        .concat(refToBuy.getState().getData().getInvoiceId().toString()) : e.getMessage());
            }


            return getDTO(refToBuy.getState().getData());
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw new Exception("Failed to buy invoiceOffer ".concat(e.getMessage()));
            } else {
                throw new Exception("Failed to buy invoiceOffer. Unknown cause");
            }
        }
    }

    public static AccountInfoDTO startAccountRegistrationFlow(CordaRPCOps proxy,
                                                              String accountName, String email, String password,
                                                              String cellphone) throws Exception {
        try {
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<AccountInfo> page = proxy.vaultQueryByWithPagingSpec(
                    AccountInfo.class, criteria,
                    new PageSpecification(1, 200));
            logger.info(" \uD83E\uDDA0 \uD83E\uDDA0 Accounts found on network:  \uD83E\uDD6C " + page.getStates().size());
            for (StateAndRef<AccountInfo> ref : page.getStates()) {
                AccountInfo info = ref.getState().getData();
                if (info.getName().equalsIgnoreCase(accountName)) {
                    logger.info("Account " + accountName + " \uD83D\uDC7F \uD83D\uDC7F already exists on the network");
                    throw new Exception("Account already exists on the network");
                }
            }

            List<NodeInfo> nodes = proxy.networkMapSnapshot();
            CordaFuture<AccountInfo> accountInfoCordaFuture = proxy.startTrackedFlowDynamic(
                    AccountRegistrationFlow.class, accountName).getReturnValue();

            AccountInfo accountInfo = accountInfoCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F Flow completed... " +
                    " \uD83D\uDC4C \uD83D\uDC4C " +
                    "\uD83D\uDC4C accountInfo returned: \uD83E\uDD4F " +
                    accountInfo.getName().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            //create user record in firebase
            try {
                UserRecord userRecord = FirebaseUtil.createUser(accountName, email, password,
                        cellphone, accountInfo.getIdentifier().getId().toString());
                logger.info("\uD83C\uDF4E \uD83C\uDF4E \uD83C\uDF4E User created on Firebase: "
                        .concat(userRecord.getDisplayName().concat(" - ").concat(userRecord.getEmail())));
            } catch (Exception e) {
                logger.error(e.getMessage());
                logger.error("Firebase fucked up ......");
                throw e;
            }

            String name = accountInfo.getHost().getName().getOrganisation();
            for (NodeInfo node : nodes) {
                Party otherParty = node.getLegalIdentities().get(0);
                if (name.equalsIgnoreCase(otherParty.getName().getOrganisation())) {
                    logger.info("\uD83D\uDD15  \uD83D\uDD15  ignore sharing - party on same node \uD83E\uDD6C ");
                    continue;
                }
                if (otherParty.getName().getOrganisation().contains("Notary")) {
                    logger.info("\uD83D\uDD15  \uD83D\uDD15 ignore sharing - this party is a Notary \uD83E\uDD6C \uD83E\uDD6C ");
                    continue;
                }
                String res = startAccountSharingFlow(proxy, otherParty, accountInfo);
                logger.info(" \uD83D\uDE0E \uD83D\uDE0E " + accountName
                        + " shared with \uD83D\uDC7F " +
                        node.getLegalIdentities().get(0).getName().getOrganisation());

            }

            AccountInfoDTO dto = new AccountInfoDTO();
            dto.setHost(accountInfo.getHost().toString());
            dto.setIdentifier(accountInfo.getIdentifier().getId().toString());
            dto.setName(accountInfo.getName());
            dto.setStatus(accountInfo.getStatus().name());

            try {
                FirebaseUtil.sendAccountMessage(dto);
                ApiFuture<DocumentReference> reference = db.collection("accounts").add(dto);
                logger.info(("\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 " +
                        "Firestore path: ").concat(reference.get().getPath()));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return dto;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private static String startAccountSharingFlow(CordaRPCOps proxy,
                                                  Party otherParty, AccountInfo account) throws Exception {
        try {
            CordaFuture<String> accountInfoCordaFuture = proxy.startFlowDynamic(
                    ShareAccountInfoFlow.class, otherParty, account).getReturnValue();
            String result = accountInfoCordaFuture.get();
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    public static InvoiceOfferDTO startInvoiceOfferFlow(CordaRPCOps proxy, InvoiceOfferDTO invoiceOffer) throws Exception {

        try {
            //todo - refactor to proper query ...
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<InvoiceState> invoiceStatePage = proxy.vaultQueryByWithPagingSpec(
                    InvoiceState.class, criteria,
                    new PageSpecification(1, 200));
            InvoiceState invoiceState = null;
            for (StateAndRef<InvoiceState> state : invoiceStatePage.getStates()) {
                if (state.getState().getData().getInvoiceId().toString().equalsIgnoreCase(invoiceOffer.getInvoiceId())) {
                    invoiceState = state.getState().getData();
                    break;
                }
            }
            if (invoiceState == null) {
                logger.warn("InvoiceState not found, \uD83D\uDC7F offer probably made on foreign node");
                throw new Exception("Invoice not found");
            }
            AccountInfo investorInfo = null;
            Vault.Page<AccountInfo> acctsPage = proxy.vaultQueryByWithPagingSpec(
                    AccountInfo.class, criteria,
                    new PageSpecification(1, 200));

            for (StateAndRef<AccountInfo> info : acctsPage.getStates()) {
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoiceOffer.getInvestor().getIdentifier())) {
                    investorInfo = info.getState().getData();
                }
            }
            if (investorInfo == null) {
                throw new Exception("Investor not found");
            }
            if (invoiceOffer.getDiscount() == 0) {
                throw new Exception("Discount not found");
            }

            double nPercentage = 100.0 - (invoiceOffer.getDiscount());
            invoiceOffer.setOfferAmount(invoiceOffer.getOriginalAmount() * (nPercentage / 100));

            return sendInvoiceOffer(proxy, invoiceOffer, invoiceState, investorInfo);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw new Exception("Failed to register invoiceOffer.  \uD83D\uDC7F possibly invoice not found");
            } else {
                throw new Exception("Failed to register invoiceOffer. Unknown cause");
            }
        }
    }

    public static void startCreateTokenFlow(CordaRPCOps proxy, AccountInfoDTO accountInfo) throws Exception {

        try {
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
            Vault.Page<AccountInfo> page = proxy.vaultQueryBy(criteria,
                    new PageSpecification(1,200), null,
                    AccountInfo.class);
            AccountInfo info = null;
            for (StateAndRef<AccountInfo> ref: page.getStates()) {
                AccountInfo m = ref.getState().getData();
                if (accountInfo.getIdentifier().equalsIgnoreCase(m.getIdentifier().getId().toString())) {
                    info = m;
                    break;
                }

            }
            if (info == null) {
                throw new Exception("Account not found");
            }

            Party party = info.getHost();
            InvoiceTokenType tokenType = new InvoiceTokenType(
                    party, party.getOwningKey(), new BigDecimal(0), new UniqueIdentifier(), 2);

            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    CreateInvoiceOfferTokenType.class, tokenType)
                    .getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDC4C " +
                    "\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F " +
                    issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
        } catch (Exception e) {
            throw new Exception("TokenType creation failed. " + e.getMessage());
        }

    }
    public static List<InvoiceOfferDTO> startInvoiceOfferFlowToAllAccounts(CordaRPCOps proxy, InvoiceOfferAllDTO all) throws Exception {

        try {
            if (all.getDiscount() == 0) {
                throw new Exception("Discount not found");
            }
            //todo - refactor to proper query ...
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<InvoiceState> invoiceStatePage = proxy.vaultQueryByWithPagingSpec(
                    InvoiceState.class, criteria,
                    new PageSpecification(1, 200));
            InvoiceState invoiceState = null;
            for (StateAndRef<InvoiceState> state : invoiceStatePage.getStates()) {
                if (state.getState().getData().getInvoiceId().toString().equalsIgnoreCase(all.getInvoiceId())) {
                    invoiceState = state.getState().getData();
                    break;
                }
            }
            if (invoiceState == null) {
                throw new Exception("Invoice not found");
            }

            Vault.Page<AccountInfo> accountInfoPage = proxy.vaultQueryByWithPagingSpec(
                    AccountInfo.class, criteria,
                    new PageSpecification(1, 200));

            AccountInfoDTO m = getAccount(proxy, all.getAccountId());
            logger.info("we have an account ... 1");
            List<InvoiceOfferDTO> offers = new ArrayList<>();
            //
            InvoiceOfferDTO invoiceOffer = new InvoiceOfferDTO();
            invoiceOffer.setInvoiceId(all.getInvoiceId());
            invoiceOffer.setInvoiceNumber(invoiceState.getInvoiceNumber());
            invoiceOffer.setOfferAmount(all.getOfferAmount());
            invoiceOffer.setDiscount(all.getDiscount());
            invoiceOffer.setSupplier(m);
            invoiceOffer.setOwner(m);
            logger.info("we have an account ... 2");
            invoiceOffer.setOriginalAmount(invoiceState.getTotalAmount().doubleValue());
            invoiceOffer.setOfferDate(new Date());
            logger.info("we have an account ... 3");
            double n = 100.0 - (invoiceOffer.getDiscount()) / 100;
            invoiceOffer.setOfferAmount(invoiceOffer.getOriginalAmount() * n);

            logger.info("\uD83D\uDC7D \uD83D\uDC7D INVOICE: ".concat(invoiceOffer.getInvoiceId())
                    .concat(" offerAmount: " + invoiceState.getTotalAmount()));
            logger.info("we have to send offer  to " + (accountInfoPage.getStates().size() - 1) + " accounts");
            for (StateAndRef<AccountInfo> info : accountInfoPage.getStates()) {
                if (info.getState().getData().getIdentifier().getId().toString().equalsIgnoreCase(all.getAccountId())) {
                    logger.info("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 Ignore this account :: \uD83D\uDE21  ".concat(info.getState().getData().getName()));
                    continue;
                }
                invoiceOffer.setInvestor(getDTO(info.getState().getData()));
                InvoiceOfferDTO offerDTO = sendInvoiceOffer(proxy, invoiceOffer,
                        invoiceState, info.getState().getData());
                offers.add(offerDTO);
            }
            return offers;
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw new Exception("Failed to add invoiceOffers. ".concat(e.getMessage()));
            } else {
                throw new Exception("Failed to add invoiceOffers. Unknown cause");
            }
        }
    }

    private static InvoiceOfferDTO sendInvoiceOffer(CordaRPCOps proxy, InvoiceOfferDTO invoiceOffer, InvoiceState invoiceState, AccountInfo investorInfo) throws Exception {
        InvoiceOfferState invoiceOfferState = new InvoiceOfferState(
                invoiceState.getInvoiceId(),
                new BigDecimal(invoiceOffer.getOfferAmount()),
                new BigDecimal(invoiceOffer.getDiscount()),
                invoiceState.getTotalAmount(),
                invoiceState.getSupplierInfo(),
                investorInfo,
                invoiceState.getSupplierInfo(),
                new Date(proxy.currentNodeTime().toEpochMilli()),
                null,
                invoiceState.getSupplierInfo().getHost().getOwningKey(),
                investorInfo.getHost().getOwningKey(), invoiceState.getInvoiceNumber(),
                invoiceState.getCustomerInfo());

        CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                InvoiceOfferFlow.class, invoiceOfferState)
                .getReturnValue();

        SignedTransaction issueTx = signedTransactionCordaFuture.get();
        logger.info("\uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDC4C " +
                "\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F " +
                issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));

        InvoiceOfferDTO offerDTO = getDTO(invoiceOfferState);
        try {
            ApiFuture<DocumentReference> reference = db.collection("invoiceOffers").add(offerDTO);
            logger.info(("\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 " +
                    "Firestore path: ").concat(reference.get().getPath()));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        FirebaseUtil.sendInvoiceOfferMessage(offerDTO);
        return offerDTO;
    }


    private static InvoiceDTO getDTO(InvoiceState state) throws Exception {
        InvoiceDTO invoice = new InvoiceDTO();
        invoice.setAmount(state.getAmount().doubleValue());
        invoice.setCustomer(getDTO(state.getCustomerInfo()));
        invoice.setSupplier(getDTO(state.getSupplierInfo()));
        invoice.setDescription(state.getDescription());
        invoice.setInvoiceId(state.getInvoiceId().toString());
        invoice.setInvoiceNumber(state.getInvoiceNumber());
        invoice.setTotalAmount(state.getTotalAmount().doubleValue());
        invoice.setValueAddedTax(state.getValueAddedTax().doubleValue());

        if (state.getSupplierPublicKey() == null) {
            throw new Exception("Supplier Public Key is null");
        }
        if (state.getCustomerPublicKey() == null) {
            throw new Exception("Customer Public Key is null");
        }
        String supplierString = Base64.getEncoder().encodeToString(state.getSupplierPublicKey().getEncoded());
        String customerString = Base64.getEncoder().encodeToString(state.getCustomerPublicKey().getEncoded());

        invoice.setSupplierPublicKey(state.getSupplierPublicKey() == null ? null : supplierString);
        invoice.setCustomerPublicKey(state.getCustomerPublicKey() == null ? null : customerString);
        invoice.setDateRegistered(state.getDateRegistered());
        return invoice;
    }

    private static InvoiceOfferDTO getDTO(InvoiceOfferState state) throws Exception {

        InvoiceOfferDTO o = new InvoiceOfferDTO();
        o.setInvoiceId(state.getInvoiceId().toString());
        o.setInvoiceNumber(state.getInvoiceNumber());
        o.setOfferAmount(state.getOfferAmount().doubleValue());
        o.setOriginalAmount(state.getOriginalAmount().doubleValue());
        o.setDiscount(state.getDiscount().doubleValue());
        o.setSupplier(getDTO(state.getSupplier()));
        o.setInvestor(getDTO(state.getInvestor()));
        o.setCustomer(getDTO(state.getCustomer()));
        if (state.getOwner() != null) {
            o.setOwner(getDTO(state.getOwner()));
        }
        if (state.getSupplierPublicKey() == null) {
            throw new Exception("Supplier Public Key is null");
        }
        if (state.getInvestorPublicKey() == null) {
            throw new Exception("Investor Public Key is null");
        }
        String supplierString = Base64.getEncoder().encodeToString(state.getSupplierPublicKey().getEncoded());
        String investorString = Base64.getEncoder().encodeToString(state.getInvestorPublicKey().getEncoded());
        o.setSupplierPublicKey(state.getSupplierPublicKey() == null ? null : supplierString);
        o.setInvestorPublicKey(state.getInvestorPublicKey() == null ? null : investorString);


        if (state.getOfferDate() != null) {
            o.setOfferDate(state.getOfferDate());
        }
        if (state.getOwnerDate() != null) {
            o.setInvestorDate(state.getOwnerDate());
        }
        return o;
    }

    private static AccountInfoDTO getDTO(AccountInfo a) {
        AccountInfoDTO info = new AccountInfoDTO();
        info.setHost(a.getHost().toString());
        info.setIdentifier(a.getIdentifier().getId().toString());
        info.setName(a.getName());
        info.setStatus(a.getStatus().name());
        return info;
    }
}
