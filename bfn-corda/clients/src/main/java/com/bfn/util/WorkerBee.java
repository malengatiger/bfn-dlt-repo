package com.bfn.util;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.dto.InvoiceDTO;
import com.bfn.dto.InvoiceOfferDTO;
import com.bfn.dto.NodeInfoDTO;
import com.bfn.flows.admin.AccountRegistrationFlow;
import com.bfn.flows.admin.ShareAccountInfoFlow;
import com.bfn.flows.invoices.BuyInvoiceOfferFlow;
import com.bfn.flows.invoices.InvoiceOfferFlow;
import com.bfn.flows.invoices.InvoiceRegistrationFlow;
import com.bfn.states.InvoiceOfferState;
import com.bfn.states.InvoiceState;
import com.google.firebase.auth.UserRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static jdk.nashorn.internal.objects.Global.print;

public class WorkerBee {
    private final static Logger logger = LoggerFactory.getLogger(WorkerBee.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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

    public static List<InvoiceDTO> getInvoiceStates(CordaRPCOps proxy) {
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page<InvoiceState> page = proxy.vaultQueryByWithPagingSpec(InvoiceState.class, criteria, new PageSpecification(1, 200));

        List<InvoiceDTO> list = new ArrayList<>();
        int cnt = 0;
        for (StateAndRef<InvoiceState> ref : page.getStates()) {
            InvoiceState m = ref.getState().getData();
            InvoiceDTO invoice = getDTO(m);
            list.add(invoice);
        }
        String m = " \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A done listing InvoiceStates:  \uD83C\uDF3A " + list.size();

        return list;
    }

    public static List<InvoiceOfferDTO> getInvoiceOfferStates(CordaRPCOps proxy, boolean consumed) {
        logger.info(" \uD83E\uDDE1 getInvoiceOfferStates consumed:  \uD83E\uDDE1 " + consumed);
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(
                consumed? Vault.StateStatus.CONSUMED : Vault.StateStatus.UNCONSUMED);
        Vault.Page<InvoiceOfferState> page = proxy.vaultQueryByWithPagingSpec(
                InvoiceOfferState.class, criteria,
                new PageSpecification(1, 200));
        List<InvoiceOfferDTO> list = new ArrayList<>();

        int cnt = 0;
        for (StateAndRef<InvoiceOfferState> ref : page.getStates()) {
            InvoiceOfferState m = ref.getState().getData();
            InvoiceOfferDTO invoice = getDTO(m);
            list.add(invoice);
        }
        String m = " \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A done listing InvoiceOfferStates:  \uD83C\uDF3A " + list.size();
        logger.info(m);
        return list;
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

        logger.info("Input Parameters; \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F InvoiceDTO: "
                + GSON.toJson(invoice) + " \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F");
        try {
            logger.info("\uD83C\uDF4F SUPPLIER: ".concat(invoice.getSupplierId()).concat("  \uD83D\uDD06  ")
                    .concat("  \uD83E\uDDE1 CUSTOMER: ").concat(invoice.getCustomerId()));

            List<StateAndRef<AccountInfo>> accounts = proxy.vaultQuery(AccountInfo.class).getStates();
            AccountInfo supplierInfo = null, customerInfo = null;
            for (StateAndRef<AccountInfo> info : accounts) {

                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getCustomerId())) {
                    customerInfo = info.getState().getData();
                    logger.info("\uD83C\uDF4F \uD83C\uDF4F Customer AccountInfo found: ".concat(info.getState().getData().getName()));
                }
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getSupplierId())) {
                    supplierInfo = info.getState().getData();
                    logger.info("\uD83C\uDF4F \uD83C\uDF4F Supplier AccountInfo found: ".concat(info.getState().getData().getName()));
                }
            }
            if (supplierInfo == null) {
                throw new Exception("Supplier is fucking missing");
            }
            if (customerInfo == null) {
                throw new Exception("Customer is bloody missing");
            }
            logger.info("\uD83D\uDC7D \uD83D\uDC7D SUPPLIER: ".concat(supplierInfo.getHost().getName().getOrganisation()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D CUSTOMER: ".concat(customerInfo.getHost().getName().getOrganisation()));
            double m = invoice.getValueAddedTax() / 100;
            logger.info("discount used: " + m);
            invoice.setTotalAmount(invoice.getAmount() + (m * invoice.getAmount()));
            InvoiceState invoiceState = new InvoiceState(UUID.randomUUID(),
                    invoice.getInvoiceNumber(),invoice.getDescription(),
                    invoice.getAmount(),invoice.getTotalAmount(),invoice.getValueAddedTax(),
                    supplierInfo,customerInfo,null,null);

            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    InvoiceRegistrationFlow.class, invoiceState).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  " +
                    "\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F "
                    + issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            InvoiceDTO mm = getDTO(invoiceState);
            logger.info(GSON.toJson(mm));
            return mm;
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw new Exception("Failed to register invoice. ".concat(e.getMessage()));
            } else {
                throw new Exception("Failed to register invoice. Unknown cause");
            }
        }
    }

    public static String startBuyInvoiceOfferFlow(CordaRPCOps proxy, String invoiceId) throws Exception {

         try {

             QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
             Vault.Page<InvoiceOfferState> page = proxy.vaultQueryByWithPagingSpec(
                     InvoiceOfferState.class, criteria,
                     new PageSpecification(1, 200));
             List<StateAndRef<InvoiceOfferState>> refs = page.getStates();
             logger.info("\uD83C\uDF4F InvoiceOffers on Node: \uD83C\uDF4F \uD83C\uDF4F " + refs.size());
             StateAndRef<InvoiceOfferState> refToBuy = null;
             for (StateAndRef<InvoiceOfferState> ref : refs) {
                 InvoiceOfferState state = ref.getState().getData();
                 if (state.getInvoiceId().toString().equalsIgnoreCase(invoiceId)) {
                     refToBuy = ref;
                     break;
                 }
             }
             if (refToBuy == null) {
                 throw new Exception("InvoiceOffer to buy not found");
             }
             logger.info(GSON.toJson(refToBuy.getState().getData().getSupplier().getName()));
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    BuyInvoiceOfferFlow.class, refToBuy).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  " +
                    "\n\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F "
                    + issueTx.getId().toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            return issueTx.getId().toString();
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
            logger.info("phone: ".concat(cellphone));
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<AccountInfo> page = proxy.vaultQueryByWithPagingSpec(
                    AccountInfo.class, criteria,
                    new PageSpecification(1, 200));
            logger.info(" \uD83E\uDDA0 \uD83E\uDDA0 Accounts found on network:  \uD83E\uDD6C " + page.getStates().size());
            for (StateAndRef<AccountInfo> ref: page.getStates()) {
                AccountInfo info = ref.getState().getData();
                if (info.getName().equalsIgnoreCase(accountName)) {
                    logger.info("Account "+accountName+" \uD83D\uDC7F \uD83D\uDC7F already exists on the network");
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
                        .concat(GSON.toJson(userRecord)));
            } catch (Exception e) {
                logger.error(e.getMessage());
                logger.error("Firebase fucked up ......");
                throw e;
            }

            String name = accountInfo.getHost().getName().getOrganisation();
            for (NodeInfo node: nodes) {
                Party otherParty = node.getLegalIdentities().get(0);
                if (name.equalsIgnoreCase(otherParty.getName().getOrganisation())) {
                    logger.info("\uD83D\uDD15  \uD83D\uDD15  ignore sharing - party on same node \uD83E\uDD6C ");
                    continue;
                }
                if (otherParty.getName().getOrganisation().contains("Notary")) {
                    logger.info("\uD83D\uDD15  \uD83D\uDD15 ignore sharing - this party is a Notary \uD83E\uDD6C \uD83E\uDD6C ");
                    continue;
                }
                String res = startAccountSharingFlow(proxy,otherParty,accountInfo);
                logger.info(" \uD83D\uDE0E \uD83D\uDE0E "+accountName
                        +" shared with \uD83D\uDC7F " +
                        node.getLegalIdentities().get(0).getName().getOrganisation());

            }

            AccountInfoDTO dto = new AccountInfoDTO();
            dto.setHost(accountInfo.getHost().toString());
            dto.setIdentifier(accountInfo.getIdentifier().getId().toString());
            dto.setName(accountInfo.getName());
            dto.setStatus(accountInfo.getStatus().name());
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
            logger.info("\uD83C\uDF4F INVOICE: ".concat(invoiceOffer.getInvoiceId()).concat("  \uD83D\uDD06  ")
                    .concat("  \uD83E\uDDE1 DISCOUNT: ").concat("" + invoiceOffer.getDiscount())
            .concat("  \uD83E\uDDE1 INVESTOR: ").concat("" + invoiceOffer.getInvestorId()));

            //todo - refactor to proper query ...
            QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            Vault.Page<InvoiceState> invoiceStatePage = proxy.vaultQueryByWithPagingSpec(
                    InvoiceState.class, criteria,
                    new PageSpecification(1, 200));
            InvoiceState invoiceState = null;
            for (StateAndRef<InvoiceState> state: invoiceStatePage.getStates()) {
                if (state.getState().getData().getInvoiceId().toString().equalsIgnoreCase(invoiceOffer.getInvoiceId())) {
                    invoiceState = state.getState().getData();
                    break;
                }
            }
            if (invoiceState == null) {
                throw new Exception("Invoice not found");
            }
            AccountInfo investorInfo = null;
            Vault.Page<AccountInfo> acctsPage = proxy.vaultQueryByWithPagingSpec(
                    AccountInfo.class, criteria,
                    new PageSpecification(1, 200));

            for (StateAndRef<AccountInfo> info : acctsPage.getStates()) {
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoiceOffer.getInvestorId())) {
                    investorInfo = info.getState().getData();
                    logger.info("\uD83C\uDF4F \uD83C\uDF4F Investor AccountInfo found: ".concat(info.getState().getData().getName()));
                }
            }
            if (investorInfo == null) {
                throw new Exception("Investor not found");
            }
            if (invoiceOffer.getDiscount() == 0) {
                throw new Exception("Discount not found");
            }
            logger.info("\uD83D\uDC7D \uD83D\uDC7D INVOICE: ".concat(invoiceOffer.getInvoiceId())
            .concat(" totalAmount: " + invoiceState.getTotalAmount()));
            logger.info("\uD83D\uDC7D \uD83D\uDC7D INVESTOR: ".concat(investorInfo.getHost().getName().getOrganisation()));

            invoiceOffer.setOfferAmount(invoiceState.getTotalAmount() *
                    ((100.0 - invoiceOffer.getDiscount())/100));

            InvoiceOfferState invoiceOfferState = new InvoiceOfferState(
                    invoiceState.getInvoiceId(),
                    invoiceOffer.getOfferAmount(),
                    invoiceOffer.getDiscount(),
                    invoiceState.getTotalAmount(),
                    invoiceState.getSupplierInfo(),
                    investorInfo,
                    invoiceState.getSupplierInfo(),
                    new Date(proxy.currentNodeTime().toEpochMilli()),
                    null, null, null);

            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    InvoiceOfferFlow.class, invoiceOfferState)
                    .getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDC4C " +
                    "\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F " +
                    issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));

            InvoiceOfferDTO m = getDTO(invoiceOfferState);
            FirebaseUtil.sendInvoiceOfferMessage(m);
//            logger.info(" \uD83E\uDDE9  \uD83E\uDDE9 Returned invoiceOffer: ".concat(GSON.toJson(m)));
            return m;
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw new Exception("Failed to register invoiceOffer. ".concat(e.getMessage()));
            } else {
                throw new Exception("Failed to register invoiceOffer. Unknown cause");
            }
        }
    }


    private static InvoiceDTO getDTO(InvoiceState state) {
        InvoiceDTO invoice = new InvoiceDTO();
        invoice.setAmount(state.getAmount());
        invoice.setCustomerId(state.getCustomerInfo().getIdentifier().getId().toString());
        invoice.setSupplierId(state.getSupplierInfo().getIdentifier().getId().toString());
        invoice.setDescription(state.getDescription());
        invoice.setInvoiceId(state.getInvoiceId().toString());
        invoice.setInvoiceNumber(state.getInvoiceNumber());
        invoice.setTotalAmount(state.getTotalAmount());
        invoice.setValueAddedTax(state.getValueAddedTax());

        invoice.setSupplierPublicKey(state.getSupplierPublicKey() == null? null : state.getSupplierPublicKey().toString());
        invoice.setCustomerPublicKey(state.getCustomerPublicKey() == null? null : state.getCustomerPublicKey().toString());
        invoice.setDateRegistered(state.getDateRegistered());
//        logger.info("Invoice State: ".concat(GSON.toJson(invoice)));
        return invoice;
    }

    private static InvoiceOfferDTO getDTO(InvoiceOfferState state) {
        AccountInfo owner = state.getOwner();
        String ownerId = null;
        if (owner != null) {
            ownerId = owner.getIdentifier().getId().toString();
        }
        InvoiceOfferDTO o = new InvoiceOfferDTO();
        o.setInvoiceId(state.getInvoiceId().toString());
        o.setOfferAmount(state.getOfferAmount());
        o.setOriginalAmount(state.getOriginalAmount());
        o.setDiscount(state.getDiscount());
        o.setSupplierId(state.getSupplier().getIdentifier().getId().toString());
        o.setInvestorId(state.getInvestor().getIdentifier().getId().toString());
        o.setOwnerId(ownerId);
        o.setSupplierPublicKey(state.getSupplierPublicKey() == null? null : state.getSupplierPublicKey().toString());
        o.setInvestorPublicKey(state.getInvestorPublicKey() == null? null : state.getInvestorPublicKey().toString());


        if (state.getOfferDate() != null) {
            o.setOfferDate(state.getOfferDate());
        }
        if (state.getOwnerDate() != null) {
            o.setInvestorDate(state.getOwnerDate());
        }
//        logger.info("InvoiceOffer State: ".concat(GSON.toJson(o)));
        return o;
    }
}
