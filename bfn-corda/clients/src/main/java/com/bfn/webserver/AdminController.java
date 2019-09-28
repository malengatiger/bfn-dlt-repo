package com.bfn.webserver;

import com.bfn.dto.*;
import com.bfn.util.DemoUtil;
import com.bfn.util.FirebaseUtil;
import com.google.firebase.auth.UserRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.vault.CriteriaExpression;
import net.corda.core.node.services.vault.QueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import com.bfn.util.WorkerBee;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/admin") // The paths for HTTP requests are relative to this base path.
public class AdminController {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    private Environment env;

    public AdminController(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A AdminController: NodeRPCConnection proxy has been injected: \uD83C\uDF3A " + proxy.nodeInfo().toString());
    }

    @GetMapping(value = "/demo", produces = "application/json")
    private DemoSummary buildDemo(@RequestParam boolean deleteFirestore) throws Exception {

        logger.info("\uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 starting DemoDataGenerator ... \uD83C\uDF4F");
        DemoSummary result = DemoUtil.start(proxy, deleteFirestore);
        logger.info("\n\n\uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 DemoUtil result: " +
                " \uD83C\uDF4F " + GSON.toJson(result)
                .concat("    \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A \uD83D\uDC99 \uD83D\uDC9C\n\n"));

        return result;
    }


    @PostMapping(value = "/startAccountRegistrationFlow", produces = "application/json")
    private AccountInfoDTO startAccountRegistrationFlow(@RequestBody UserDTO user) throws Exception {

        return WorkerBee.startAccountRegistrationFlow(proxy,user.getName(),user.getEmail(),user.getPassword(), user.getCellphone());
    }

    @GetMapping(value = "getAccounts")
    private List<AccountInfoDTO> getAccounts() {
        return WorkerBee.getAccounts(proxy);
    }

    @GetMapping(value = "listFirestoreNodes")
    private List<NodeInfoDTO> listFirestoreNodes() throws ExecutionException, InterruptedException {
        return WorkerBee.listFirestoreNodes();
    }
/*
@GetMapping(value = "/states", produces = arrayOf("text/plain"))
    private fun states() = proxy.vaultQueryBy<ContractState>().states.toString()
 */
@GetMapping(value = "/getStates", produces = "application/json")
private List<String>  getStates() {
    String msg = "\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A AdminController:BFN Web API pinged: " + new Date().toString()
            + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A";
    logger.info(msg);
   return WorkerBee.getStates(proxy);
}
    @GetMapping(value = "getInvoiceStates")
    public List<InvoiceDTO> getInvoiceStates(@RequestParam(value = "consumed", required=false) boolean consumed,
                                             @RequestParam(value = "accountId", required=false) String accountId) throws Exception {
        return WorkerBee.getInvoiceStates(proxy, accountId, consumed);
    }
    @GetMapping(value = "getInvoiceOfferStates")
    public List<InvoiceOfferDTO> getInvoiceOfferStates(@RequestParam(value = "consumed", required=false) boolean consumed,
                                                       @RequestParam(value = "accountId", required=false) String accountId) throws Exception {

        return WorkerBee.getInvoiceOfferStates(proxy, accountId, consumed);
    }
    @GetMapping(value = "getUser")
    public UserRecord getUser(@RequestParam(value = "email", required=false) String email) throws Exception {

        UserRecord record = FirebaseUtil.getUser(email);
//        if (record == null) {
//            throw new Exception("User not found: ".concat(email));
//        }
        return record;
    }
    @GetMapping(value = "getAccount")
    public AccountInfoDTO getAccount(@RequestParam(value = "accountId") String accountId) throws Exception {

        return  WorkerBee.getAccount(proxy,accountId);
    }
    @GetMapping(value = "writeNodesToFirestore")
    public List<NodeInfoDTO> writeNodesToFirestore() throws Exception {
        if (env == null) {
            throw new Exception("Environment variables not available");
        } else {
            logger.info("\uD83C\uDF40 \uD83C\uDF40 \uD83C\uDF40 " +
                    "Environment variables available \uD83C\uDF40 ");
        }
        return  WorkerBee.writeNodesToFirestore(proxy, env);
    }


    @GetMapping(value = "/hello", produces = "text/plain")
    private String hello() {
        logger.info("/ requested. will say hello  \uD83D\uDC9A  \uD83D\uDC9A  \uD83D\uDC9A");
        return "\uD83D\uDC9A  BFNWebApi: AdminController says  \uD83E\uDD6C HELLO WORLD!  \uD83D\uDC9A  \uD83D\uDC9A";
    }

    @GetMapping(value = "/ping", produces = "application/json")
    private String ping() {
        String msg = "\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A AdminController:BFN Web API pinged: " + new Date().toString()
                + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A";
        logger.info(msg);
        NodeInfo nodeInfo = proxy.nodeInfo();
        logger.info("\uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 node pinged: "
                + nodeInfo.getLegalIdentities().get(0).getName().toString()
                + proxy.getNetworkParameters().toString() + " \uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 ");

        return "\uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A  AdminController: node pinged: " +
                nodeInfo.getLegalIdentities().get(0).getName().toString() +
                " \uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A " +
                proxy.getNetworkParameters().toString();
    }
    @GetMapping(value = "/getDashboardData", produces = "application/json")
    private DashboardData getDashboardData() {

        return WorkerBee.getDashboardData(proxy);

    }
    @GetMapping(value = "/nodes", produces = "application/json")
    private List<NodeInfoDTO> listNodes() {

        return WorkerBee.listNodes(proxy);
    }

    @GetMapping(value = "/notaries", produces = "application/json")
    private List<String> listNotaries() {
        return WorkerBee.listNotaries(proxy);
    }

    @GetMapping(value = "/flows", produces = "application/json")
    private List<String> listFlows() {
        return WorkerBee.listFlows(proxy);
    }


    private class PingResult {
        String message;
        String nodeInfo;

        PingResult(String message, String nodeInfo) {
            this.message = message;
            this.nodeInfo = nodeInfo;
        }
    }
}
