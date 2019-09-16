package com.bfn.webserver;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.dto.InvoiceDTO;
import com.bfn.dto.InvoiceOfferDTO;
import com.bfn.dto.NodeInfoDTO;
import com.bfn.flows.invoices.InvoiceOfferFlow;
import com.bfn.states.InvoiceOfferState;
import com.bfn.states.InvoiceState;
import com.bfn.util.TheUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/supplier") // The paths for HTTP requests are relative to this base path.
public class SupplierController {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(SupplierController.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public SupplierController(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A SupplierController: NodeRPCConnection proxy has been injected: \uD83C\uDF3A " + proxy.nodeInfo().toString());
    }

    @GetMapping(value = "/hello", produces = "text/plain")
    private String hello() {
        logger.info("/ requested. will say hello  \uD83D\uDC9A  \uD83D\uDC9A  \uD83D\uDC9A");
        return " \uD83E\uDD6C  \uD83E\uDD6C BFNWebApi: SupplierController says  \uD83E\uDD6C HELLO WORLD!  \uD83D\uDC9A  \uD83D\uDC9A";
    }

    @GetMapping(value = "/ping", produces = "application/json")
    private String ping() {
        String msg = "\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A SupplierController:BFN Web API pinged: " + new Date().toString()
                + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A";

        logger.info("\uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 " + proxy.getNetworkParameters().toString() + " \uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 ");
        PingResult pingResult = new PingResult(msg, proxy.nodeInfo().toString());
        logger.info("\uD83C\uDF3A SupplierController: node pinged: \uD83C\uDF3A  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 : " + proxy.nodeInfo().getLegalIdentities().get(0).getName().toString() + " \uD83E\uDDE9");

        return GSON.toJson(pingResult);
    }

    @GetMapping(value = "/nodes", produces = "application/json")
    private List<NodeInfoDTO> listNodes() {

        return TheUtil.listNodes(proxy);

    }
    @GetMapping(value = "/getAccountInfoByID", produces = "application/json")
    private AccountInfoDTO getAccountInfoByID(@RequestParam String id) throws Exception {
        //todo - learn how to use criteria or SQL queries
        try {
//            QueryCriteria generalCriteria = new VaultQueryCriteria(Vault.StateStatus.ALL);
//            FieldInfo attributeId = getField("identifier", AccountInfo.class);
//            logger.info("getField executed: ".concat(attributeId.getName()));
//            CriteriaExpression criteriaExpression = Builder.equal(attributeId, new UniqueIdentifier(id));
//
//
//            //QueryCriteria queryCriteria = new VaultQueryCriteria(Vault.StateStatus.ALL, ImmutableSet.of(AccountInfo.class));
//            QueryCriteria queryCriteria = new VaultCustomQueryCriteria<>(criteriaExpression, Vault.StateStatus.ALL, ImmutableSet.of(AccountInfo.class));
//                    QueryCriteria criteria = generalCriteria.and(queryCriteria);
            List<StateAndRef<AccountInfo>> results = proxy.vaultQuery(AccountInfo.class).getStates();
            if (results.size() == 0) {
                throw new Exception("AccountInfo not found: ".concat(id));
            }
            logger.info("\uD83D\uDC99 \uD83D\uDC99 \uD83D\uDC99 AccountInfo's found: " + results.size() + " \uD83D\uDC99");
            AccountInfo info = null;
            for (StateAndRef<AccountInfo> ref: results) {
                if (ref.getState().getData().getIdentifier().getId().toString().equalsIgnoreCase(id)) {
                    info = ref.getState().getData();
                }
            }
            if (info == null) {
                throw new Exception(" \uD83E\uDDE1 AccountInfo not found");
            }
            AccountInfoDTO dto = new AccountInfoDTO(
                    info.getIdentifier().getId().toString(),
                    info.getHost().toString(),
                    info.getName(), info.getStatus().name());
            logger.info(" \uD83E\uDDE1  \uD83E\uDDE1 AccountInfo found  \uD83E\uDDE1 ".concat(GSON.toJson(dto)));
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() == null) {
                throw new Exception("getAccountInfoByID encountered unknown error");
            } else {
                throw new Exception(e.getMessage());
            }
        }
    }


    @PostMapping(value = "startRegisterInvoiceFlow")
    public InvoiceDTO startRegisterInvoiceFlow(@RequestBody InvoiceDTO invoice) throws Exception {

        logger.info("Input Parameters; \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F InvoiceDTO: " + GSON.toJson(invoice) + " \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F");
        return TheUtil.startRegisterInvoiceFlow(proxy,invoice);
    }

    @PostMapping(value = "startInvoiceOfferFlow")
    public InvoiceOfferDTO startInvoiceOfferFlow(@RequestBody InvoiceOfferDTO invoiceOffer) throws Exception {

        logger.info("Input Parameters; \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F InvoiceOfferDTO: " + GSON.toJson(invoiceOffer) + " \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F");
        try {
            InvoiceOfferDTO m =TheUtil.startInvoiceOfferFlow(proxy,invoiceOffer);
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

    @GetMapping(value = "getInvoiceStates")
    public List<InvoiceDTO> getInvoiceStates() {
        return TheUtil.getInvoiceStates(proxy);
    }
    @GetMapping(value = "getInvoiceOfferStates")
    public List<InvoiceOfferDTO> getInvoiceOfferStates() {

        return TheUtil.getInvoiceOfferStates(proxy);
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
