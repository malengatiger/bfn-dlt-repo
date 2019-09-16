package com.bfn.flows.admin;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import kotlin.Unit;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
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
public class ShareAccountInfoFlow extends FlowLogic<String> {
    private final static Logger logger = LoggerFactory.getLogger(ShareAccountInfoFlow.class);
    private final AccountInfo accountInfo;
    private  final Party party;

    public ShareAccountInfoFlow(AccountInfo accountInfo, Party party) {
        this.accountInfo = accountInfo;
        this.party = party;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {
        ServiceHub hub = getServiceHub();
        logger.info(" \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A ShareAccountInfoWithAllNodesFlow call: instant: "
                .concat(hub.getClock().instant().toString())
        .concat(" parm: ").concat(accountInfo.getName()));
        List<NodeInfo> list = hub.getNetworkMapCache().getAllNodes();
        AccountService accountService = hub.cordaService(KeyManagementBackedAccountService.class);
        List<StateAndRef<AccountInfo>> states = accountService.allAccounts();

        try {
            CordaFuture<Unit> future = accountService.shareAccountInfoWithParty(accountInfo.getIdentifier().getId(), party);
            Unit result = null;
            result = future.get();
            if (result != null) {
                logger.info("We have a result from sharing future result: ".concat(result.toString()));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return "\uD83C\uDFC8  \uD83C\uDFC8 Accounts shared: " + states.size() + " with " + list.size();
    }
}
