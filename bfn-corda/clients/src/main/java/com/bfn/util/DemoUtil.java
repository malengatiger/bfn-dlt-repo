package com.bfn.util;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.dto.InvoiceDTO;
import com.bfn.dto.InvoiceOfferDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DemoUtil {

    private final static Logger logger = LoggerFactory.getLogger(DemoUtil.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static CordaRPCOps proxy;
    private static List<AccountInfoDTO> suppliers, customers, investors;
    private static DemoSummary demoSummary = new DemoSummary();

    public static DemoSummary start(CordaRPCOps mProxy) throws Exception {
        proxy = mProxy;
        logger.info("\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 DemoUtil started ...  \uD83D\uDD06 \uD83D\uDD06 will list network components");
        demoSummary.setStarted(new Date().toString());
        long start = System.currentTimeMillis();
        suppliers = new ArrayList<>();
        customers = new ArrayList<>();
        investors = new ArrayList<>();
        List nodes = TheUtil.listNodes(proxy);
        demoSummary.setNumberOfNodes(nodes.size());
        List flows = TheUtil.listFlows(proxy);
        demoSummary.setNumberOfFlows(flows.size());

        //start data generation
        registerSupplierAccounts();
        registerCustomerAccounts();
        registerInvestorAccounts();

        registerInvoices();

        List<AccountInfoDTO> list = TheUtil.getAccounts(proxy);
        logger.info(" \uD83C\uDF4E  \uD83C\uDF4E Total Number of Accounts on Node after sharing:" +
                " \uD83C\uDF4E  \uD83C\uDF4E " + list.size());
        demoSummary.setNumberOfAccounts(list.size());

        long end = System.currentTimeMillis();
        demoSummary.setEnded(new Date().toString());
        demoSummary.setElapsedSeconds((end - start)/1000);
        return demoSummary;
    }
//    private static void shareAccounts() throws Exception {
//        List<NodeInfo> nodes = proxy.networkMapSnapshot();
//        logger.info("\n\n\uD83E\uDDA0 \uD83E\uDDA0 shareAccounts: Nodes running: " + nodes.size());
//        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
//        Vault.Page<AccountInfo> page = proxy.vaultQueryByWithPagingSpec(AccountInfo.class,
//                criteria,new PageSpecification(1,200));
//        String myNode = proxy.nodeInfo().getLegalIdentities().get(0).getName().toString();
//        List<StateAndRef<AccountInfo>> list = page.getStates();
//        logger.info("\uD83E\uDDA0 \uD83E\uDDA0  Accounts on "+myNode+" Node: " + list.size());
//        for (NodeInfo nodeInfo: nodes) {
//            String name = nodeInfo.getLegalIdentities().get(0).getName().toString();
//            Party otherParty = nodeInfo.getLegalIdentities().get(0);
//            if (name.equalsIgnoreCase(myNode)) {
//                logger.info("\uD83D\uDD15  \uD83D\uDD15  ignore sharing - party on same node \uD83E\uDD6C ");
//                continue;
//            }
//            if (name.contains("Notary")) {
//                logger.info("\uD83D\uDD15  \uD83D\uDD15 ignore sharing - this party is a Notary \uD83E\uDD6C \uD83E\uDD6C ");
//                continue;
//            }
//
//            for (StateAndRef<AccountInfo> accountInfoStateAndRef: list) {
//                String result = TheUtil.startAccountSharingFlow(proxy,otherParty,accountInfoStateAndRef);
//                logger.info("\uD83C\uDF81 Result from sharing account: ".concat(result));
//            }
//        }
//    }

    private static void registerSupplierAccounts() throws Exception {
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerSupplierAccounts started ...  " +
                "\uD83D\uDD06 \uD83D\uDD06 ");
        String key = "" + random.nextInt(100);
        String name = proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation();
        
        AccountInfoDTO supplier1 = TheUtil.startAccountRegistrationFlow(proxy,name.concat(".Supplier One".concat("#").concat(key)));
        AccountInfoDTO supplier2 = TheUtil.startAccountRegistrationFlow(proxy,name.concat(".Supplier Two".concat("#").concat(key)));

        suppliers.add(supplier1);
        suppliers.add(supplier2);

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerSupplierAccounts complete ..." +
                "  \uD83D\uDD06 \uD83D\uDD06 added "+suppliers.size()+" accounts");

    }
    private static void registerCustomerAccounts() throws Exception {
        String name = proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation();
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerCustomerAccounts started ...  \uD83D\uDD06 \uD83D\uDD06 ");
        String key = "" + random.nextInt(100);
        AccountInfoDTO customer1 = TheUtil.startAccountRegistrationFlow(proxy,name.concat(".Customer One LLC".concat("#").concat(key)));
        AccountInfoDTO customer2 = TheUtil.startAccountRegistrationFlow(proxy,name.concat(".Customer Two LLC".concat("#").concat(key)));

        customers.add(customer1);
        customers.add(customer2);

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 registerCustomerAccounts complete ...  " +
                "\uD83D\uDD06 \uD83D\uDD06 added "+customers.size()+" accounts");

    }
    private static void registerInvestorAccounts() throws Exception {
        String name = proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation();
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerInvestorAccounts started ... " +
                " \uD83D\uDD06 \uD83D\uDD06");
        String key = "" + random.nextInt(100);

        AccountInfoDTO investor1 = TheUtil.startAccountRegistrationFlow(proxy,name.concat(".Investor One Inc.".concat("#").concat(key)));
        AccountInfoDTO investor2 = TheUtil.startAccountRegistrationFlow(proxy,name.concat(".Investor Two LLC".concat("#").concat(key)));

        investors.add(investor1);
        investors.add(investor2);

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 registerInvestorAccounts complete ...  " +
                "\uD83D\uDD06 \uD83D\uDD06 added "+investors.size()+"  accounts");

    }

    private static Random random = new Random(System.currentTimeMillis());
    private static void registerInvoices() throws Exception {

//        List<AccountInfoDTO> list = TheUtil.getAccounts(proxy);
//        logger.info("\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerInvoices:" +
//                "  \uD83D\uDD06 \uD83D\uDD06 Total Number of Accounts on ALL NODES:  \uD83D\uDC8E " + list.size() + "  \uD83D\uDC8E ");
//        suppliers.clear();
//        for (AccountInfoDTO m: list) {
//            if (m.getName().contains("Supplier")) {
//                suppliers.add(m);
//            }
//        }
//        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerInvoices:" +
//                "  \uD83D\uDD06 \uD83D\uDD06 " + suppliers.size() + " suppliers");
//        customers.clear();
//        for (AccountInfoDTO m: list) {
//            if (m.getName().contains("Customer")) {
//                customers.add(m);
//            }
//        }
//        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerInvoices:" +
//                "  \uD83D\uDD06 \uD83D\uDD06 " + customers.size() + " customers");
//        investors.clear();
//        for (AccountInfoDTO m: list) {
//            if (m.getName().contains("Investor")) {
//                investors.add(m);
//            }
//        }
//        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerInvoices:" +
//                "  \uD83D\uDD06 \uD83D\uDD06 " + investors.size() + " investors");

        for (AccountInfoDTO supplier: suppliers ) {
            for (AccountInfoDTO customer: customers) {
                InvoiceDTO m = new InvoiceDTO();
                m.setInvoiceNumber("INV_" + System.currentTimeMillis());
                m.setSupplierId(supplier.getIdentifier());
                m.setCustomerId(customer.getIdentifier());
                int num = random.nextInt(100);
                if (num == 0) num = 92;
                m.setAmount(num * 1000.00);
                m.setValueAddedTax(10.0);
                m.setTotalAmount(m.getAmount() * 1.10);
                m.setDescription("Demo Invoice at ".concat(new Date().toString()));
                m.setDateRegistered(new Date());

                InvoiceDTO invoice = TheUtil.startRegisterInvoiceFlow(proxy,m);
                double discount = random.nextInt(25) * 1.0;
                for (AccountInfoDTO investor: investors) {
                    try {
                        registerInvoiceOffer(invoice, supplier, investor, discount);
                    } catch (Exception e) {

                    }
                }
            }
        }

        List<InvoiceDTO> invoiceStates = TheUtil.getInvoiceStates(proxy);
        logger.info(" \uD83C\uDF4A  \uD83C\uDF4A "+invoiceStates.size()+" InvoiceStates on node ...  \uD83C\uDF4A ");
        demoSummary.setNumberOfInvoices(invoiceStates.size());

        List<InvoiceOfferDTO> list2 = TheUtil.getInvoiceOfferStates(proxy, false);
        demoSummary.setNumberOfInvoiceOffers(list2.size());
        logger.info(" \uD83C\uDF4A  \uD83C\uDF4A "+list2.size()+" InvoiceOfferStates on node ...  \uD83C\uDF4A ");

    }
    private static void registerInvoiceOffer(InvoiceDTO invoice, AccountInfoDTO supplier,
                                             AccountInfoDTO investor, double discount) throws Exception {
        logger.info("\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerInvoiceOffer started ..." +
                "  \uD83D\uDD06 \uD83D\uDD06 ");

        InvoiceOfferDTO m = new InvoiceOfferDTO();
        m.setInvoiceId(invoice.getInvoiceId());
        m.setSupplierId(supplier.getIdentifier());
        m.setOwnerId(supplier.getIdentifier());
        m.setInvestorId(investor.getIdentifier());
        m.setOfferDate(new Date());
        m.setDiscount(discount);
        if (m.getDiscount() == 0) {
            m.setDiscount(5.8);
        }
        m.setOfferAmount(invoice.getTotalAmount() * ((100.0 - m.getDiscount()) / 100));
        m.setOriginalAmount(invoice.getTotalAmount());

        TheUtil.startInvoiceOfferFlow(proxy,m);
    }
}

