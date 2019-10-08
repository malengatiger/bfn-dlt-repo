package com.bfn.webserver;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;
/**
 * Wraps an RPC connection to a Corda node.
 * <p>
 * The RPC connection is configured using command line arguments.
 */
@Component
public class NodeRPCConnection implements AutoCloseable {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(NodeRPCConnection.class);
    CordaRPCOps proxy;

    @Value("${config.rpc.host}")
    private String host;
    // The RPC port of the node we are connecting to.
    @Value("${config.rpc.username}")
    private String username;
    // The username for logging into the RPC client.
    @Value("${config.rpc.password}")
    private String password;
    // The password for logging into the RPC client.
    @Value("${config.rpc.port}")
    private int rpcPort;

    private CordaRPCConnection rpcConnection;

    @PostConstruct
    public void initialiseNodeRPCConnection() {

        logger.info(" \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9  Environment vars: \uD83C\uDF4F host: "
                + host
                + " \uD83C\uDF4F username: " + username + " \uD83C\uDF4F password: "
                + password + " \uD83C\uDF4F rpcPort: " + rpcPort);
        assert rpcPort > 0;

        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(host, rpcPort);
        logger.info(" \uD83E\uDD8B  \uD83E\uDD8B  \uD83E\uDD8B rpcAddress ok. " + rpcAddress.toString().concat(" \uD83C\uDF3A \uD83C\uDF3A"));
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        logger.info(" \uD83E\uDD8B  \uD83E\uDD8B  \uD83E\uDD8B rpcClient ok. \uD83C\uDF3A \uD83C\uDF3A ".concat(rpcClient.toString()));
        assert password != null;
        assert username != null;
        rpcConnection = rpcClient.start(username, password);
        logger.info(" \uD83E\uDD8B  \uD83E\uDD8B  \uD83E\uDD8B rpcConnection ok: \uD83C\uDF3A".concat(" \uD83C\uDF3A \uD83C\uDF3A"));
        proxy = rpcConnection.getProxy();
        logger.info(" \uD83E\uDD8B  \uD83E\uDD8B  \uD83E\uDD8B proxy ok. \uD83C\uDF3A \uD83C\uDF3A "
                .concat(proxy.nodeInfo().getLegalIdentities().get(0).getName().toString()));

        logger.info("\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 initialise: Corda ServerProtocolVersion: " +
                "\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A " + rpcConnection.getServerProtocolVersion());
        logger.info("\uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 initialise: getLegalIdentities: " +
                "\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A " + proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation() + "  \uD83D\uDC9A  \uD83D\uDC9A");
    }

    @PreDestroy
    public void close() {
        rpcConnection.notifyServerAndClose();
    }
}
