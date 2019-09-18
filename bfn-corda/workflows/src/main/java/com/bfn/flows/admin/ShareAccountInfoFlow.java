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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@StartableByRPC
public class ShareAccountInfoFlow extends FlowLogic<String> {
    private final static Logger logger = LoggerFactory.getLogger(ShareAccountInfoFlow.class);
    private final Party otherParty;
    private final StateAndRef<AccountInfo> account;

    public ShareAccountInfoFlow(Party otherParty, StateAndRef<AccountInfo> account) {
        this.otherParty = otherParty;
        this.account = account;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {
        ServiceHub hub = getServiceHub();
        logger.info(" \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A  \uD83C\uDF3A ShareAccountInfoFlow call started" );
        AccountService accountService = hub.cordaService(KeyManagementBackedAccountService.class);
        try {
            logger.info(" \uD83C\uDF38  \uD83C\uDF38 ... sharing "
                    .concat(account.getState().getData().getName()).concat(" with \uD83E\uDD6C \uD83E\uDD6C "
                            .concat(otherParty.getName().toString())));

            CompletableFuture<Unit> future = accountService.shareAccountInfoWithParty(
                    account.getState().getData().getIdentifier().getId(), otherParty).toCompletableFuture();
            Unit result = future.get();
            if (result != null) {
                logger.info(" \uD83D\uDE0E  \uD83D\uDE0E We have a GOOD result from sharing future result: "
                        .concat(result.toString()));
            } else {
                logger.info(" \uD83D\uDE0E  \uD83D\uDE0E We have a \uD83D\uDC7F NULL result" +
                        " \uD83D\uDC7F from sharing future result: ");
            }

        } catch (InterruptedException e) {
            logger.error("\uD83D\uDC7F \uD83D\uDC7F InterruptedException: ".concat(e.getMessage()));
            throw new FlowException("InterruptedException: Unable to share accounts");
        } catch (ExecutionException e) {
            logger.error("\uD83D\uDC7F \uD83D\uDC7F ExecutionException: ".concat(e.getMessage()));
            throw new FlowException("ExecutionException: Unable to share accounts");
        }

        return "\uD83C\uDFC8  \uD83C\uDFC8 Account shared: "
                + account.getState().getData().getName();
    }
}
