package com.bfn.util;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.dto.DemoSummary;
import com.bfn.dto.InvoiceDTO;
import com.bfn.dto.InvoiceOfferDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.corda.core.messaging.CordaRPCOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DemoUtil {

    private final static Logger logger = LoggerFactory.getLogger(DemoUtil.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static CordaRPCOps proxy;
    private static List<AccountInfoDTO> suppliers, customers, investors;
    private static DemoSummary demoSummary = new DemoSummary();

    public static DemoSummary start(CordaRPCOps mProxy, boolean deleteFirestore) throws Exception {
        proxy = mProxy;
        logger.info("\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 DemoUtil started ...  \uD83D\uDD06 \uD83D\uDD06 will list network components");
        demoSummary.setStarted(new Date().toString());
        long start = System.currentTimeMillis();
        suppliers = new ArrayList<>();
        customers = new ArrayList<>();
        investors = new ArrayList<>();
        List nodes = WorkerBee.listNodes(proxy);
        demoSummary.setNumberOfNodes(nodes.size());
        List flows = WorkerBee.listFlows(proxy);
        demoSummary.setNumberOfFlows(flows.size());

        //delete Firestore data
        if (deleteFirestore) {
            try {
                FirebaseUtil.deleteUsers();
                FirebaseUtil.deleteCollections();
            } catch (Exception e) {
                logger.warn("Firebase shit bombed");
            }
        }
        //start data generation
        registerSupplierAccounts();
        registerCustomerAccounts();
        registerInvestorAccounts();

        registerInvoices();

        List<AccountInfoDTO> list = WorkerBee.getAccounts(proxy);
        logger.info(" \uD83C\uDF4E  \uD83C\uDF4E Total Number of Accounts on Node after sharing:" +
                " \uD83C\uDF4E  \uD83C\uDF4E " + list.size());
        demoSummary.setNumberOfAccounts(list.size());

        long end = System.currentTimeMillis();
        demoSummary.setEnded(new Date().toString());
        demoSummary.setElapsedSeconds((end - start)/1000);
        return demoSummary;
    }

    private static void registerSupplierAccounts() throws Exception {
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerSupplierAccounts started ...  " +
                "\uD83D\uDD06 \uD83D\uDD06 ");
        String key = "" + random.nextInt(100);
        String phone = getPhone();
        String name = proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation();

        AccountInfoDTO supplier1 = WorkerBee.startAccountRegistrationFlow(proxy,name.concat(".SupplierOne")
                        .concat("#").concat(key),
                "supplier".concat(phone).concat("@gmail.com"),"pass123",phone);
        phone = getPhone();
        AccountInfoDTO supplier2 = WorkerBee.startAccountRegistrationFlow(proxy,name.concat(".SupplierTwo")
                        .concat("#").concat(key),
                "supplier".concat(phone).concat("@gmail.com"),"pass123",phone);

        suppliers.add(supplier1);
        suppliers.add(supplier2);

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerSupplierAccounts complete ..." +
                "  \uD83D\uDD06 \uD83D\uDD06 added "+suppliers.size()+" accounts");

    }
    private static void registerCustomerAccounts() throws Exception {
        String name = proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation();
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerCustomerAccounts started ...  \uD83D\uDD06 \uD83D\uDD06 ");
        String key = "" + random.nextInt(100);

        String phone = getPhone();
        AccountInfoDTO customer1 = WorkerBee.startAccountRegistrationFlow(proxy,name.concat(".CustomerOne")
                        .concat("#").concat(key),
                "customer".concat(phone).concat("@gmail.com"),"pass123",phone);
        phone = getPhone();
        AccountInfoDTO customer2 = WorkerBee.startAccountRegistrationFlow(proxy,name.concat(".CustomerTwo")
                        .concat("#").concat(key),
                "customer".concat(phone).concat("@gmail.com"),"pass123",phone);
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

        String phone = getPhone();
        AccountInfoDTO investor1 = WorkerBee.startAccountRegistrationFlow(proxy,name.concat(".InvestorOne")
                        .concat("#").concat(key),
                "investor".concat(phone).concat("@gmail.com"),"pass123",phone);
        phone = getPhone();
        AccountInfoDTO investor2 = WorkerBee.startAccountRegistrationFlow(proxy,name.concat(".InvestorTwo")
                        .concat("#").concat(key),
                "investor".concat(phone).concat("@gmail.com"),"pass123",phone);
        investors.add(investor1);
        investors.add(investor2);

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 registerInvestorAccounts complete ...  " +
                "\uD83D\uDD06 \uD83D\uDD06 added "+investors.size()+"  accounts");

    }

    static String getPhone() {
        StringBuilder sb = new StringBuilder();
        sb.append("27");
        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));

        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));

        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));
        sb.append(random.nextInt(9));
        return sb.toString();
    }
    private static Random random = new Random(System.currentTimeMillis());
    private static void registerInvoices() throws Exception {

        for (AccountInfoDTO supplier: suppliers ) {
            for (AccountInfoDTO customer: customers) {
                InvoiceDTO m = new InvoiceDTO();
                m.setInvoiceNumber("INV_" + System.currentTimeMillis());
                m.setSupplier(supplier);
                m.setCustomer(customer);
                int num = random.nextInt(100);
                if (num == 0) num = 92;
                m.setAmount(num * 1000.00);
                m.setValueAddedTax(10.0);
                m.setTotalAmount(m.getAmount() * 1.10);
                m.setDescription("Demo Invoice at ".concat(new Date().toString()));
                m.setDateRegistered(new Date());

                InvoiceDTO invoice = WorkerBee.startInvoiceRegistrationFlow(proxy,m);
                double discount = random.nextInt(25) * 1.0;
                for (AccountInfoDTO investor: investors) {
                    try {
                        registerInvoiceOffer(invoice, supplier, investor, discount);
                    } catch (Exception e) {

                    }
                }
            }
        }

        List<InvoiceDTO> invoiceStates = WorkerBee.getInvoiceStates(proxy, null, false);
        logger.info(" \uD83C\uDF4A  \uD83C\uDF4A "+invoiceStates.size()+" InvoiceStates on node ...  \uD83C\uDF4A ");
        demoSummary.setNumberOfInvoices(invoiceStates.size());

        List<InvoiceOfferDTO> list2 = WorkerBee.getInvoiceOfferStates(proxy, null,false);
        demoSummary.setNumberOfInvoiceOffers(list2.size());
        logger.info(" \uD83C\uDF4A  \uD83C\uDF4A "+list2.size()+" InvoiceOfferStates on node ...  \uD83C\uDF4A ");

    }
    private static void registerInvoiceOffer(InvoiceDTO invoice, AccountInfoDTO supplier,
                                             AccountInfoDTO investor, double discount) throws Exception {
        logger.info("\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerInvoiceOffer started ..." +
                "  \uD83D\uDD06 \uD83D\uDD06 ");

        InvoiceOfferDTO m = new InvoiceOfferDTO();
        m.setInvoiceId(invoice.getInvoiceId());
        m.setSupplier(supplier);
        m.setOwner(supplier);
        m.setInvestor(investor);
        m.setOfferDate(new Date());
        m.setDiscount(discount);
        if (m.getDiscount() == 0) {
            m.setDiscount(5.8);
        }
        m.setOfferAmount(invoice.getTotalAmount() * ((100.0 - m.getDiscount()) / 100));
        m.setOriginalAmount(invoice.getTotalAmount());

        WorkerBee.startInvoiceOfferFlow(proxy,m);
    }
}

