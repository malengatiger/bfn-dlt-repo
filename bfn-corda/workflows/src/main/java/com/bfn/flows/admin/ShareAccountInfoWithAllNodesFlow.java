package com.bfn.flows.admin;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import kotlin.Unit;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionState;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.ServiceHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

@StartableByRPC
public class ShareAccountInfoWithAllNodesFlow extends FlowLogic<String> {
    private final static Logger logger = LoggerFactory.getLogger(ShareAccountInfoWithAllNodesFlow.class);
    private final String parm;

    public ShareAccountInfoWithAllNodesFlow(String parm) {
        this.parm = parm;
        logger.info(" \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A  constructor fired, parm: ".concat(parm));
    }

    @Override
    @Suspendable
    public String call() throws FlowException {
        ServiceHub hub = getServiceHub();
        logger.info(" \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A ShareAccountInfoWithAllNodesFlow call: instant: "
                .concat(hub.getClock().instant().toString())
        .concat(" parm: ").concat(parm));
        List<NodeInfo> list = hub.getNetworkMapCache().getAllNodes();
        AccountService accountService = hub.cordaService(KeyManagementBackedAccountService.class);
        List<StateAndRef<AccountInfo>> states = accountService.allAccounts();

        NodeInfo myInfo = hub.getMyInfo();
        logger.info("\uD83C\uDF3A accountService : allAccounts: ".concat(" \uD83C\uDF3A " + states.size() + " \uD83C\uDF3A"));
        try {
            for (StateAndRef<AccountInfo> ref : states) {
                TransactionState<AccountInfo> state = ref.getState();
                for (NodeInfo nodeInfo : list) {
                    if (myInfo.getLegalIdentities().get(0).getName().toString()
                            .equalsIgnoreCase(nodeInfo.getLegalIdentities().get(0).getName().toString())) {
                        logger.info("\uD83C\uDF3A ignore sharing - on same node: "
                                .concat(nodeInfo.getLegalIdentities().get(0).getName().toString()));
                        continue;
                    }
                    if (nodeInfo.getLegalIdentities().get(0).getName().toString()
                            .contains("Notary")) {
                        logger.info("\uD83C\uDF3A ignore sharing - this is a notary: "
                                .concat(nodeInfo.getLegalIdentities().get(0).getName().toString()));
                        continue;
                    }
                    Party party = nodeInfo.getLegalIdentities().get(0);
                    logger.info(" \uD83C\uDFC8  \uD83C\uDFC8 Sharing with party: ".concat(party.getName().toString()));
                    CordaFuture<Unit> future = accountService.shareAccountInfoWithParty(state.getData().getIdentifier().getId(), party);
                    Unit result = future.get();
                    if (result != null) {
                        logger.info("We have a result from sharing future result: ".concat(result.toString()));
                    }
                    logger.info(" \uD83C\uDFC8  \uD83C\uDFC8 Shared account: ".concat(state.getData().getIdentifier().getId().toString())
                            .concat(" with:  \uD83D\uDC99 node: ").concat(party.getName().toString()));
                }
            }
        } catch (Exception e) {
            return "\uD83C\uDFC8  \uD83C\uDFC8 Accounts NOT shared: EXCEPTION: ".concat(e.getMessage());
//            throw new FlowException("......... Something wrong, Senor!");
        }

        return "\uD83C\uDFC8  \uD83C\uDFC8 Accounts shared: " + states.size() + " with " + list.size();
    }
}
