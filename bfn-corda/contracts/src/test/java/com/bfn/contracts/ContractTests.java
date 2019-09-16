package com.bfn.contracts;

import com.bfn.states.SupplierState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final MockServices ledgerServices = new MockServices();

    private final TestIdentity alice = new TestIdentity(new CordaX500Name("OneConnectSuppliers", "Johannesburg","ZA"));
    private final SupplierState supplierState = new SupplierState(alice.getParty(),
            "Alice","aloce@oneconnect.co.za",
            "099 877 5643","tbd", "Pharma, Retail");
    @Test
    public void contractIsSupplierContract() {

        assert (new SupplierContract() instanceof Contract);
    }
    @Test
    public void supplierContractsRequiresZeroInputsInTheTransaction() {
        transaction(ledgerServices, tx -> {
            tx.input(SupplierContract.ID, supplierState);
            tx.output(SupplierContract.ID,supplierState);
            tx.command(alice.getPublicKey(),new SupplierContract.Register());
            tx.fails();
            return  null;

        });
    }
}
