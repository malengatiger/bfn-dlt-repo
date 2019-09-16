package com.bfn.util;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.dto.InvoiceDTO;
import com.bfn.dto.InvoiceOfferDTO;
import com.bfn.dto.NodeInfoDTO;
import com.bfn.flows.admin.AccountRegistrationFlow;
import com.bfn.flows.invoices.InvoiceOfferFlow;
import com.bfn.flows.invoices.InvoiceRegistrationFlow;
import com.bfn.states.InvoiceOfferState;
import com.bfn.states.InvoiceState;
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
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TheUtil {
    private final static Logger logger = LoggerFactory.getLogger(TheUtil.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public  static List<NodeInfoDTO> listNodes(CordaRPCOps proxy) {

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
        for (StateAndRef<AccountInfo> ref: accounts) {
            cnt++;
//            logger.info(" \uD83C\uDF3A AccountInfo: #".concat("" + cnt + " :: ").concat(ref.getState().getData().toString()
//                    .concat(" \uD83E\uDD4F ")));
            AccountInfo info = ref.getState().getData();
            AccountInfoDTO dto = new AccountInfoDTO(info.getIdentifier().getId().toString(),
                    info.getHost().toString(),info.getName(),info.getStatus().name());
            list.add(dto);
        }
        String msg = "\uD83C\uDF3A  \uD83C\uDF3A done listing accounts:  \uD83C\uDF3A " + list.size();
        logger.info(msg);
        return list;
    }
    public static List<InvoiceDTO> getInvoiceStates(CordaRPCOps proxy) {
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page<InvoiceState> page = proxy.vaultQueryByWithPagingSpec(InvoiceState.class, criteria, new PageSpecification(1,200));

        List<InvoiceDTO> list = new ArrayList<>();
        int cnt = 0;
        for (StateAndRef<InvoiceState> ref: page.getStates()) {
            InvoiceState m = ref.getState().getData();
            InvoiceDTO invoice = new InvoiceDTO(
                    m.getInvoiceId().toString(),
                    m.getInvoiceNumber(),
                    m.getDescription(),
                    m.getAmount(),
                    m.getTotalAmount(),
                    m.getValueAddedTax(),
                    m.getSupplierInfo().getIdentifier().getId().toString(),
                    m.getCustomerInfo().getIdentifier().getId().toString());
            list.add(invoice);
        }
        String m = " \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A done listing InvoiceStates:  \uD83C\uDF3A " + list.size();

        return list;
    }
    public static List<InvoiceOfferDTO> getInvoiceOfferStates(CordaRPCOps proxy) {

        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Vault.Page<InvoiceOfferState> page = proxy.vaultQueryByWithPagingSpec(InvoiceOfferState.class, criteria, new PageSpecification(1,200));
        List<InvoiceOfferDTO> list = new ArrayList<>();

        int cnt = 0;
        for (StateAndRef<InvoiceOfferState> ref: page.getStates()) {
            InvoiceOfferState m = ref.getState().getData();
            InvoiceOfferDTO invoice = new InvoiceOfferDTO(m.getInvoiceId().toString(),
                    m.getOfferAmount(),
                    m.getDiscount(),
                    m.getSupplier().getIdentifier().getId().toString(),
                    m.getInvestor().getIdentifier().getId().toString(),
                    m.getOwner() != null? m.getOwner().getIdentifier().getId().toString() : null);
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
        for (String info: flows) {
            cnt++;
            logger.info("\uD83E\uDD4F \uD83E\uDD4F #$"+cnt+" \uD83E\uDD6C BFN Corda Flow:  \uD83E\uDD4F" + info + "   \uD83C\uDF4E ");
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
    public static InvoiceDTO startRegisterInvoiceFlow(CordaRPCOps proxy, InvoiceDTO invoice) throws Exception {

        logger.info("Input Parameters; \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F InvoiceDTO: " + GSON.toJson(invoice) + " \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F");
        try {
            logger.info("\uD83C\uDF4F SUPPLIER: ".concat(invoice.getSupplierId()).concat("  \uD83D\uDD06  ")
                    .concat("  \uD83E\uDDE1 CUSTOMER: ").concat(invoice.getCustomerId()));

            List<StateAndRef<AccountInfo>> accounts = proxy.vaultQuery(AccountInfo.class).getStates();
            logger.info(" \uD83C\uDF4F \uD83C\uDF4F AccountInfo's found by vaultQuery: \uD83D\uDD34 " + accounts.size() + " \uD83D\uDD34 ");
            AccountInfo supplierInfo = null, customerInfo = null;
            for (StateAndRef<AccountInfo> info: accounts) {

                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getCustomerId())) {
                    customerInfo = info.getState().getData();
                    logger.info(" \uD83C\uDF4F \uD83C\uDF4F Customer AccountInfo found: ".concat(info.getState().getData().getName()));
                }
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getSupplierId())) {
                    supplierInfo = info.getState().getData();
                    logger.info(" \uD83C\uDF4F \uD83C\uDF4F Supplier AccountInfo found: ".concat(info.getState().getData().getName()));
                }
            }
            if (supplierInfo == null) {
                throw new Exception("Supplier is fucking missing");
            }
            if (customerInfo == null) {
                throw new Exception("Customer is bloody missing");
            }
            InvoiceState invoiceState = new InvoiceState(
                    invoice.getInvoiceNumber(),
                    invoice.getDescription(),
                    invoice.getAmount(),
                    invoice.getTotalAmount(),
                    invoice.getValueAddedTax(),
                    new Date(proxy.currentNodeTime().toEpochMilli()),
                    supplierInfo,customerInfo,
                    UUID.randomUUID());

            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    InvoiceRegistrationFlow.class, invoiceState).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  " +
                    "\n\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F "
                    + issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            logger.info("\uD83E\uDD4F \uD83E\uDD4F returned invoiceState: \uD83E\uDD4F \uD83E\uDD4F ".concat(GSON.toJson(getDTO(invoiceState))));
            return getDTO(invoiceState);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw new Exception("Failed to register invoice. ".concat(e.getMessage()));
            } else {
                throw new Exception("Failed to register invoice. Unknown cause");
            }
        }
    }

    public static AccountInfoDTO startAccountRegistrationFlow(CordaRPCOps proxy, String accountName) throws ExecutionException, InterruptedException {
        try {
            logger.info("\uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35  startAccountRegistrationFlow: Input Parameters: ".concat(accountName));

            CordaFuture<AccountInfo> accountInfoCordaFuture = proxy.startTrackedFlowDynamic(
                    AccountRegistrationFlow.class, accountName).getReturnValue();

            logger.info("\uD83C\uDF4F AccountRegistrationFlow started ... \uD83C\uDF4F \uD83C\uDF4F waiting for signedTransaction ....");
            AccountInfo accountInfo = accountInfoCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F AccountRegistrationFlow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  \n\uD83D\uDC4C \uD83D\uDC4C " +
                    "\uD83D\uDC4C \uD83D\uDC4C  accountInfo returned: \uD83E\uDD4F " +
                    accountInfo.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));


            AccountInfoDTO dto = new AccountInfoDTO();
            dto.setHost(accountInfo.getHost().toString());
            dto.setIdentifier(accountInfo.getIdentifier().getId().toString());
            dto.setName(accountInfo.getName());
            dto.setStatus(accountInfo.getStatus().name());
            return dto;
//
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
    public static InvoiceOfferDTO startInvoiceOfferFlow(CordaRPCOps proxy,InvoiceOfferDTO invoiceOffer) throws Exception {

        logger.info("Input Parameters; \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F InvoiceOfferDTO: " + GSON.toJson(invoiceOffer) + " \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F");
        try {
            logger.info("\uD83C\uDF4F SUPPLIER: ".concat(invoiceOffer.getSupplierId()).concat("  \uD83D\uDD06  ")
                    .concat("  \uD83E\uDDE1 INVESTOR: ").concat(invoiceOffer.getInvestorId()));

            List<StateAndRef<AccountInfo>> accounts = proxy.vaultQuery(AccountInfo.class).getStates();
            logger.info(" \uD83C\uDF4F \uD83C\uDF4F AccountInfo's found by vaultQuery: \uD83D\uDD34 " + accounts.size() + " \uD83D\uDD34 ");
            AccountInfo supplierInfo = null, investorInfo = null;
            for (StateAndRef<AccountInfo> info: accounts) {

                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoiceOffer.getInvestorId())) {
                    investorInfo = info.getState().getData();
                    logger.info(" \uD83C\uDF4F \uD83C\uDF4F Investor AccountInfo found: ".concat(info.getState().getData().getName()));
                }
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoiceOffer.getSupplierId())) {
                    supplierInfo = info.getState().getData();
                    logger.info(" \uD83C\uDF4F \uD83C\uDF4F Supplier AccountInfo found: ".concat(info.getState().getData().getName()));
                }
            }
            if (supplierInfo == null) {
                throw new Exception("Supplier is fucking missing");
            }
            if (investorInfo == null) {
                throw new Exception("Investor is bloody missing");
            }
            invoiceOffer.setOfferDate(new Date());
            InvoiceOfferState invoiceOfferState = new InvoiceOfferState(
                    UUID.fromString(invoiceOffer.getInvoiceId()),
                    invoiceOffer.getOfferAmount(),
                    invoiceOffer.getDiscount(),
                    supplierInfo,
                    investorInfo,
                    supplierInfo,
                    new Date(proxy.currentNodeTime().toEpochMilli()),
                    null);
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    InvoiceOfferFlow.class, invoiceOfferState).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  \n\uD83D\uDC4C " +
                    "\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F " +
                    issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));

            InvoiceOfferDTO m = getDTO(invoiceOfferState);
            logger.info(" \uD83E\uDDE9  \uD83E\uDDE9 Returned invoiceOffer: ".concat(GSON.toJson(m)));
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
        InvoiceDTO invoice = new InvoiceDTO(
                state.getInvoiceId().toString(),
                state.getInvoiceNumber(),
                state.getDescription(),
                state.getAmount(),
                state.getTotalAmount(),
                state.getValueAddedTax(),
                state.getSupplierInfo().getIdentifier().getId().toString(),
                state.getCustomerInfo().getIdentifier().getId().toString());
        invoice.setDateRegistered(state.getDateRegistered());
        return invoice;
    }
    private static InvoiceOfferDTO getDTO(InvoiceOfferState state) {
        AccountInfo owner = state.getOwner();
        String ownerId = null;
        if (owner != null) {
            ownerId = owner.getIdentifier().getId().toString();
        }
        InvoiceOfferDTO invoiceOffer = new InvoiceOfferDTO(
                state.getInvoiceId().toString(),
                state.getOfferAmount(),
                state.getDiscount(),
                state.getSupplier().getIdentifier().getId().toString(),
                state.getInvestor().getIdentifier().getId().toString(), ownerId );

        if (state.getOfferDate() !=  null) {
            invoiceOffer.setOfferDate(state.getOfferDate());
        }
        if (state.getOwnerDate() !=  null) {
            invoiceOffer.setInvestorDate(state.getOwnerDate());
        }
        return invoiceOffer;
    }
}
