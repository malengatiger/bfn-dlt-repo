package com.bfn.webserver;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.dto.InvoiceDTO;
import com.bfn.dto.InvoiceOfferDTO;
import com.bfn.dto.NodeInfoDTO;
import com.bfn.util.DemoSummary;
import com.bfn.util.DemoUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NetworkParameters;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import com.bfn.util.TheUtil;

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

    public AdminController(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A AdminController: NodeRPCConnection proxy has been injected: \uD83C\uDF3A " + proxy.nodeInfo().toString());
    }

    @GetMapping(value = "/demo", produces = "application/json")
    private DemoSummary buildDemo() throws Exception {

        logger.info("\uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 starting DemoDataGenerator ... \uD83C\uDF4F");
        DemoSummary result = DemoUtil.start(proxy);
        logger.info("\n\n\uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35 DemoUtil result: " +
                " \uD83C\uDF4F " + GSON.toJson(result)
                .concat("    \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A \uD83D\uDC99 \uD83D\uDC9C\n\n"));

        return result;
    }

    @PostMapping(value = "/startAccountRegistrationFlow", produces = "application/json")
    private AccountInfoDTO startAccountRegistrationFlow(@RequestParam String accountName) throws Exception {

        AccountInfoDTO accountInfoDTO = TheUtil.startAccountRegistrationFlow(proxy, accountName);
        //share account
        shareAccounts();
        return accountInfoDTO;
    }

    @GetMapping(value = "getAccounts")
    private List<AccountInfoDTO> getAccounts() {
        return TheUtil.getAccounts(proxy);
    }

    @GetMapping(value = "shareAccounts")
    private String shareAccounts() throws Exception {
        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        logger.info("\n\n\uD83E\uDDA0 \uD83E\uDDA0 shareAccounts: Nodes running: " + nodes.size());
        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
        Vault.Page<AccountInfo> page = proxy.vaultQueryByWithPagingSpec(AccountInfo.class,
                criteria,new PageSpecification(1,200));
        String myNode = proxy.nodeInfo().getLegalIdentities().get(0).getName().toString();
        List<StateAndRef<AccountInfo>> list = page.getStates();
        logger.info("\uD83E\uDDA0 \uD83E\uDDA0  Accounts on Node: " + list.size());
        for (NodeInfo nodeInfo: nodes) {
            String name = nodeInfo.getLegalIdentities().get(0).getName().toString();
            Party otherParty = nodeInfo.getLegalIdentities().get(0);
            if (name.equalsIgnoreCase(myNode)) {
                logger.info("\uD83D\uDD15  \uD83D\uDD15  ignore sharing - party on same node \uD83E\uDD6C ");
                continue;
            }
            if (name.contains("Notary")) {
                logger.info("\uD83D\uDD15  \uD83D\uDD15 ignore sharing - this party is a Notary \uD83E\uDD6C \uD83E\uDD6C ");
                continue;
            }

            for (StateAndRef<AccountInfo> ref: list) {
                String result = TheUtil.startAccountSharingFlow(proxy,otherParty,ref);
                logger.info("\uD83C\uDF81 Result from sharing account ".concat(result));
            }
        }
        return "Account Sharing DONE. shared  \uD83D\uDD37 " + list.size() + "  \uD83D\uDD37 accounts";
    }


    @GetMapping(value = "getInvoiceStates")
    public List<InvoiceDTO> getInvoiceStates() {
        return TheUtil.getInvoiceStates(proxy);
    }

    @GetMapping(value = "getInvoiceOfferStates")
    public List<InvoiceOfferDTO> getInvoiceOfferStates() {
        return TheUtil.getInvoiceOfferStates(proxy);
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

    @GetMapping(value = "/nodes", produces = "application/json")
    private List<NodeInfoDTO> listNodes() {

        return TheUtil.listNodes(proxy);
    }

    @GetMapping(value = "/notaries", produces = "application/json")
    private List<String> listNotaries() {
        return TheUtil.listNotaries(proxy);
    }

    @GetMapping(value = "/flows", produces = "application/json")
    private List<String> listFlows() {
        return TheUtil.listFlows(proxy);
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
