package com.bfn.util;

import com.bfn.dto.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class DemoUtil {

    private final static Logger logger = LoggerFactory.getLogger(DemoUtil.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static CordaRPCOps proxy;
    private static List<AccountInfoDTO> suppliers, customers, investors;
    private static DemoSummary demoSummary = new DemoSummary();

    public static DemoSummary generateLocalNodeData(CordaRPCOps mProxy,
                                                    boolean deleteFirestore) throws Exception {
        proxy = mProxy;
        logger.info("\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 DemoUtil started ...  \uD83D\uDD06 \uD83D\uDD06 will list network components");
        NodeInfo myNode = proxy.nodeInfo();
        if (myNode.getLegalIdentities().get(0).getName().getOrganisation().contains("Notary")) {
            throw new Exception("Cannot add demo data to Notary");
        }
        if (myNode.getLegalIdentities().get(0).getName().getOrganisation().contains("Regulator")) {
            throw new Exception("Cannot add demo data to Regulator");
        }
        suppliers = new ArrayList<>();
        customers = new ArrayList<>();
        investors = new ArrayList<>();

        DemoSummary summary = new DemoSummary();
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
        summary.setNumberOfAccounts(list.size());
        return summary;
    }
    static List<NodeInfoDTO> nodes;
    static List<AccountInfoDTO> accounts;
    public static DemoSummary startNodes(CordaRPCOps mProxy, Environment env) throws Exception {
        proxy = mProxy;
        long start = System.currentTimeMillis();
        demoSummary.setStarted(new Date().toString());
        nodes = WorkerBee.listFirestoreNodes();
        if (nodes.isEmpty()) {
            nodes = WorkerBee.writeNodesToFirestore(proxy,env);
        }
        demoSummary.setNumberOfNodes(nodes.size());
        List flows = WorkerBee.listFlows(proxy);
        demoSummary.setNumberOfFlows(flows.size());
        logger.info(" \uD83C\uDF4E  \uD83C\uDF4E " + nodes.size()
                + " BFN Nodes");
        logger.info(" \uD83C\uDF4E  \uD83C\uDF4E " + flows.size()
                + " BFN Flows");

        generateLocalNodeData(proxy, true);
        NodeInfo nodeInfo = mProxy.nodeInfo();
        int cnt = 0;
        for (NodeInfoDTO dto : nodes) {
            String name = dto.getAddresses().get(0);
            if (nodeInfo.getLegalIdentities().get(0).getName().toString()
                    .equalsIgnoreCase(name)) {
                logger.info("\n\uD83C\uDF36 \uD83C\uDF36 Ignoring Local Node - no data to generate");
                continue;
            }
            if (dto.getAddresses().get(0).contains("Notary")) {
                logger.info("\n\uD83C\uDF36 \uD83C\uDF36 Ignoring Notary Node - no data to generate");
                continue;
            }
            if (dto.getAddresses().get(0).contains("Regulator")) {
                logger.info("\n\uD83C\uDF36 \uD83C\uDF36 Ignoring Regulator Node - no data to generate");
                continue;
            }
            try {
                executeForeignNodeDemoData(dto);
                cnt++;

            } catch (Exception e) {
                logger.error(" \uD83D\uDC7F  \uD83D\uDC7F  \uD83D\uDC7F Foreign demo data failed", e);
            }
        }

        long end = System.currentTimeMillis();
        demoSummary.setEnded(new Date().toString());
        demoSummary.setElapsedSeconds((end - start) / 1000);
        demoSummary.setDashboardData(getRegulatorDashboard());
        logger.info("\uD83C\uDF81 \uD83C\uDF81 Foreign Nodes Demo Data Generated; NODES: \uD83D\uDC99 " + cnt + " \uD83D\uDC99 " );
        return demoSummary;
    }

    private static DashboardData getRegulatorDashboard() throws Exception {
        NodeInfoDTO node = null;
        for (NodeInfoDTO x: nodes) {
            if (x.getAddresses().get(0).contains("Regulator")) {
                node = x;
                break;
            }
        }
        if (node == null) {
            throw new Exception("Regulator not found");
        }

        String nodeUrl = node.getWebAPIUrl() + "admin/getDashboardData";
        HttpURLConnection con = callNode(nodeUrl);
        DashboardData summary;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            summary = GSON.fromJson(response.toString(), DashboardData.class);

            logger.info("\uD83E\uDD1F \uD83E\uDD1F \uD83E\uDD1F " +
                    "Response from Regulator: \uD83E\uDD1F SUMMARY:: "+ node.getAddresses().get(0) + " \uD83E\uDD1F "
                    + GSON.toJson(summary) + "\n\n");
            return summary;
        }
    }
    private static void sendOfferToOtherNodes(InvoiceOfferDTO offer, AccountInfoDTO account) {
        logger.info("\uD83C\uDF3C \uD83C\uDF3C \uD83C\uDF3C ️.... sendOfferToOtherNodes ".concat(account.getName()).concat(" invoiceId: ")
        .concat(offer.getInvoiceId()).concat(" amt: ").concat("" + offer.getOfferAmount()));
        accounts = WorkerBee.getAccounts(proxy);
        for (NodeInfoDTO node: nodes) {
            if (node.getAddresses().get(0).contains(account.getHost())) {
                continue;
            }
            if (node.getAddresses().get(0).contains("Regulator")) {
                continue;
            }
            if (node.getAddresses().get(0).contains("Notary")) {
                continue;
            }
            logger.info("\uD83D\uDC7D \uD83D\uDC7D \uD83D\uDC7D Processing accounts on Node: "
                    + node.getAddresses() + " " + node.getWebAPIUrl());
            //get accounts for just this node
            List<AccountInfoDTO> list = new ArrayList<>();
            for (AccountInfoDTO m: accounts) {
                if (!m.getHost().equalsIgnoreCase(account.getHost())) {
                    list.add(m);
                }
            }
            for (AccountInfoDTO m: list) {
                if (m.getIdentifier().equalsIgnoreCase(offer.getInvestor().getIdentifier())) {
                    continue;
                }
                int x = random.nextInt(10);
                if (x > 5) {
                    continue;
                }
                InvoiceOfferDTO dto = offer;
                dto.setInvestor(m);
                dto.setOfferDate(new Date());
                try {
                    WorkerBee.startInvoiceOfferFlow(proxy, dto);
                    logger.info(" \uD83C\uDF3A  \uD83C\uDF3A " + account.getName() + " from " +
                            account.getHost() + (" \uD83C\uDF3C \uD83C\uDF3C \uD83C\uDF3C ️" +
                            "Offer sent to \uD83C\uDF4E   \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F ")
                            .concat(m.getName()).concat(" at ").concat(m.getHost())
                            .concat("  \uD83E\uDD6C invoiceId: ")
                            .concat("" + offer.getOfferAmount()));

                } catch (Exception e) {
                    logger.error(" \uD83D\uDC7F  \uD83D\uDC7F  \uD83D\uDC7F Failed to send offer to remote investor", e);
                }
            }
        }
    }
    private static void executeForeignNodeDemoData(NodeInfoDTO node) throws Exception {
        logger.info("\n\n\uD83E\uDD1F \uD83E\uDD1F \uD83E\uDD1F " +
                "Node Demo Data to Generate: " + node.getWebAPIUrl());
        String nodeUrl = node.getWebAPIUrl() + "admin/demo?deleteFirestore=false";
        HttpURLConnection con = callNode(nodeUrl);
        DemoSummary summary;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            summary = GSON.fromJson(response.toString(), DemoSummary.class);
            logger.info("\uD83E\uDD1F \uD83E\uDD1F \uD83E\uDD1F " +
                    "Response from Demo: \uD83E\uDD1F SUMMARY: NODE: " + node.getAddresses().get(0) + " \uD83E\uDD1F "
                    + GSON.toJson(summary) + "\n\n");
        }
    }

    @NotNull
    private static HttpURLConnection callNode(String nodeUrl) throws Exception {
        URL url = new URL(nodeUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "*/*");
        con.setDoOutput(true);

        int code = con.getResponseCode();
        logger.info("\uD83E\uDD1F \uD83E\uDD1F \uD83E\uDD1F " +
                "Node Call response code: \uD83D\uDE21 " + code + " \uD83D\uDE21  - " + nodeUrl);
        if (code != 200) {
            throw new Exception("Failed with status code: " + code);
        }
        return con;
    }

    private static void registerSupplierAccounts() throws Exception {
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerSupplierAccounts started ...  " +
                "\uD83D\uDD06 \uD83D\uDD06 ");
        String phone = getPhone();
        try {
            AccountInfoDTO supplier1 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
                    "supplier".concat(phone).concat("@gmail.com"), "pass123", phone);
            suppliers.add(supplier1);
        } catch (Exception e1) {
            logger.warn("Unable to add account - probable duplicate name");
        }
        phone = getPhone();
        try {
            AccountInfoDTO supplier2 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
                    "supplier".concat(phone).concat("@gmail.com"), "pass123", phone);
            suppliers.add(supplier2);
        } catch (Exception e1) {
            logger.warn("Unable to add account - probable duplicate name");
        }
        phone = getPhone();
        try {
            AccountInfoDTO supplier3 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
                    "supplier".concat(phone).concat("@gmail.com"), "pass123", phone);
            suppliers.add(supplier3);
        } catch (Exception e1) {
            logger.warn("Unable to add account - probable duplicate name");
        }
//        phone = getPhone();
//        try {
//            AccountInfoDTO supplier4 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
//                    "supplier".concat(phone).concat("@gmail.com"), "pass123", phone);
//            suppliers.add(supplier4);
//        } catch (Exception e1) {
//            logger.warn("Unable to add account - probable duplicate name");
//        }


        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerSupplierAccounts complete ..." +
                "  \uD83D\uDD06 \uD83D\uDD06 added " + suppliers.size() + " accounts");

    }

    private static void registerCustomerAccounts() throws Exception {
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 " +
                "registerCustomerAccounts started ...  \uD83D\uDD06 \uD83D\uDD06 ");
        String phone = getPhone();
        try {
            AccountInfoDTO customer1 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
                    "customer".concat(phone).concat("@gmail.com"), "pass123", phone);
            customers.add(customer1);
        } catch (Exception e1) {
            logger.warn("Unable to add account - probable duplicate name");
        }
        phone = getPhone();
        try {
            AccountInfoDTO customer2 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
                    "customer".concat(phone).concat("@gmail.com"), "pass123", phone);
            customers.add(customer2);
        } catch (Exception e1) {
            logger.warn("Unable to add account - probable duplicate name");
        }
//        phone = getPhone();
//        try {
//            AccountInfoDTO customer3 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
//                    "customer".concat(phone).concat("@gmail.com"), "pass123", phone);
//            customers.add(customer3);
//        } catch (Exception e1) {
//            logger.warn("Unable to add account - probable duplicate name");
//        }
//        phone = getPhone();
//        try {
//            AccountInfoDTO customer4 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
//                    "customer".concat(phone).concat("@gmail.com"), "pass123", phone);
//            customers.add(customer4);
//        } catch (Exception e1) {
//            logger.warn("Unable to add account - probable duplicate name");
//        }

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 registerCustomerAccounts complete ...  " +
                "\uD83D\uDD06 \uD83D\uDD06 added " + customers.size() + " accounts");

    }

    private static void registerInvestorAccounts() throws Exception {
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 " +
                "registerInvestorAccounts started ... " +
                " \uD83D\uDD06 \uD83D\uDD06");
        String phone = getPhone();
        try {
            AccountInfoDTO investor1 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
                    "investor".concat(phone).concat("@gmail.com"), "pass123", phone);
            investors.add(investor1);
        } catch (Exception e1) {
            logger.warn("Unable to add account - probable duplicate name");
        }
        phone = getPhone();
        try {
            AccountInfoDTO investor2 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
                    "investor".concat(phone).concat("@gmail.com"), "pass123", phone);
            investors.add(investor2);
        } catch (Exception e1) {
            logger.warn("Unable to add account - probable duplicate name");
        }
//        phone = getPhone();
//        try {
//            AccountInfoDTO investor3 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
//                    "investor".concat(phone).concat("@gmail.com"), "pass123", phone);
//            investors.add(investor3);
//        } catch (Exception e1) {
//            logger.warn("Unable to add account - probable duplicate name");
//        }
//        phone = getPhone();
//        try {
//            AccountInfoDTO investor4 = WorkerBee.startAccountRegistrationFlow(proxy, getRandomName(),
//                    "investor".concat(phone).concat("@gmail.com"), "pass123", phone);
//            investors.add(investor4);
//        } catch (Exception e1) {
//            logger.warn("Unable to add account - probable duplicate name");
//        }

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 registerInvestorAccounts complete ...  " +
                "\uD83D\uDD06 \uD83D\uDD06 added " + investors.size() + "  accounts");

    }

    private static String getPhone() {
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

        for (AccountInfoDTO supplier : suppliers) {
            for (AccountInfoDTO customer : customers) {
                InvoiceDTO m = new InvoiceDTO();
                m.setInvoiceNumber("INV_" + System.currentTimeMillis());
                m.setSupplier(supplier);
                m.setCustomer(customer);
                int num = random.nextInt(500);
                if (num == 0) num = 92;
                m.setAmount(num);
                m.setValueAddedTax(15.0);
                m.setTotalAmount(num * 1.15);
                m.setDescription("Demo Invoice at ".concat(new Date().toString()));
                m.setDateRegistered(new Date());

                InvoiceDTO invoice = WorkerBee.startInvoiceRegistrationFlow(proxy, m);
                double discount = random.nextInt(25) * 1.0;
                for (AccountInfoDTO investor : investors) {
                    try {
                        registerInvoiceOffer(invoice, supplier, investor, discount);
                    } catch (Exception e) {

                    }
                }
            }
        }

        List<InvoiceDTO> invoiceStates = WorkerBee.getInvoiceStates(proxy, null, false);
        logger.info(" \uD83C\uDF4A  \uD83C\uDF4A " + invoiceStates.size() + " InvoiceStates on node ...  \uD83C\uDF4A ");
        demoSummary.setNumberOfInvoices(invoiceStates.size());

        List<InvoiceOfferDTO> list2 = WorkerBee.getInvoiceOfferStates(proxy, null, false);
        demoSummary.setNumberOfInvoiceOffers(list2.size());
        logger.info(" \uD83C\uDF4A  \uD83C\uDF4A " + list2.size() + " InvoiceOfferStates on node ...  \uD83C\uDF4A ");

    }

    private static List<InvoiceOfferDTO> nodeInvoiceOffers = new ArrayList<>();

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
            m.setDiscount(3.5);
        }
        double n = 100.0 - (m.getDiscount()) / 100;
        m.setOfferAmount(n);
        m.setOriginalAmount(invoice.getTotalAmount());

        InvoiceOfferDTO offer = WorkerBee.startInvoiceOfferFlow(proxy, m);
        nodeInvoiceOffers.add(offer);
        sendOfferToOtherNodes(offer, supplier);
    }

    static List<String> names = new ArrayList<>();
    static HashMap<String, String> map = new HashMap<>();


    static String getRandomName() throws Exception {
        names.add("Jones Pty Ltd");
        names.add("Nkosi Associates");
        names.add("Maddow Enterprises");
        names.add("Xavier Inc.");
        names.add("House Inc.");
        names.add("Washington Brookes LLC");
        names.add("Johnson Associates Pty Ltd");
        names.add("Khulula Ltd");
        names.add("Innovation Partners");
        names.add("Peach Enterprises");
        names.add("Petersen Ventures Inc");
        names.add("Nixon Associates LLC");
        names.add("NamibianCool Inc.");
        names.add("BrothersFX Inc");
        names.add("Jabula Associates Pty Ltd");
        names.add("Graystone Khambule Ltd");
        names.add("Craighall Investments Ltd");
        names.add("Robert Grayson Associates");
        names.add("KZN Wildlife Pty Ltd");
        names.add("Kumar Enterprises Ltd");
        names.add("KrugerX Steel");
        names.add("TrainServices Pros Ltd");
        names.add("Topper PanelBeaters Ltd");
        names.add("Pelosi PAC LLC");
        names.add("Blackridge Inc.");
        names.add("Soweto Engineering Works Pty Ltd");
        names.add("Soweto Bakeries Ltd");
        names.add("BlackStone Partners Ltd");
        names.add("Constitution Associates LLC");
        names.add("Gauteng Manufacturers Ltd");
        names.add("Bidenstock Pty Ltd");
        names.add("Innovation Solutions Pty Ltd");
        names.add("Schiff Ventures Ltd");
        names.add("Process Innovation Partners");
        names.add("TrendSpotter Inc.");
        names.add("KnightRider Inc.");
        names.add("Fantastica Technology Inc.");
        names.add("Flickenburg Associates Pty Ltd");
        names.add("Cyber Operations Ltd");
        names.add("WorkerBees Inc.");
        names.add("FrickerRoad LLC.");
        names.add("Mamelodi Hustlers Pty Ltd");
        names.add("Wallace Incorporated");
        names.add("Peachtree Solutions Ltd");
        names.add("InnovateSpecialists Inc");
        names.add("DealMakers Pty Ltd");
        names.add("Clarity Solutions Inc");
        names.add("UK Holdings Ltd");
        names.add("Lauraine Pty Ltd");
        names.add("Paradigm Partners Inc");
        names.add("Washington Partners LLC");
        names.add("Motion Specialists Inc");
        names.add("OpenFlights Pty Ltd");
        names.add("ProServices Pty Ltd");
        names.add("TechnoServices Inc.");
        names.add("BrokerBoy Inc.");
        names.add("GermanTree Services Ltd");
        names.add("ShiftyRules Inc");
        names.add("BrookesBrothers Inc");
        names.add("PresidentialServices Pty Ltd");
        names.add("LawBook LLC");
        names.add("CampaignTech LLC");
        names.add("Tutankhamen Ventures Ltd");
        names.add("CrookesAndTugs Inc.");
        names.add("Coolidge Enterprises Inc");
        names.add("ProGuards Pty Ltd");
        names.add("BullFinch Ventures Ltd");
        names.add("ProGears Pty Ltd");
        names.add("HoverClint Ltd");
        names.add("KrugerBuild Pty Ltd");
        names.add("Treasure Hunters Inc");
        names.add("Kilimanjaro Consultants Ltd");
        names.add("Communications Brokers Ltd");
        names.add("VisualArts Inc");
        names.add("TownshipBusiness Ltd");
        names.add("HealthServices Pty Ltd");
        names.add("Macoute Professionals Ltd");
        names.add("Melber Pro Brokers Inc");
        names.add("Bronkies Park Pty Ltd");
        names.add("WhistleBlowers Inc.");
        names.add("Charles Mignon Pty Ltd");
        names.add("IntelligenceMaker Inc.");
        names.add("CroMagnon Industries");
        names.add("Status Enterprises LLC");
        names.add("Things Inc.");
        names.add("Rainmakers Ltd");
        names.add("Forensic Labs Ltd");
        names.add("DLT TechStars Inc");
        names.add("CordaBrokers Pty Ltd");

        String name = names.get(random.nextInt(names.size() - 1));
        if (map.containsKey(name)) {
            throw new Exception("Random name collision");
        } else {
            map.put(name, name);
        }

        return name;
    }

    static int randomCount;
}

