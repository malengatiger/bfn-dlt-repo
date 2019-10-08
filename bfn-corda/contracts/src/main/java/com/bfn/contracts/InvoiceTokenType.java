package com.bfn.contracts;

import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.List;

@BelongsToContract(InvoiceTokenTypeContract.class)
public class InvoiceTokenType extends EvolvableTokenType {
    private final Party maintainer;
    private final PublicKey investorKey;
    private final BigDecimal amount;
    private final UniqueIdentifier identifier;
    private final int fractionDigits;

    public InvoiceTokenType(Party maintainer, PublicKey investorKey, BigDecimal amount, UniqueIdentifier identifier, int fractionDigits) {
        this.maintainer = maintainer;
        this.investorKey = investorKey;
        this.amount = amount;
        this.identifier = identifier;
        this.fractionDigits = fractionDigits;
    }

    @Override
    public int getFractionDigits() {
        return fractionDigits;
    }

    @NotNull
    @Override
    public List<Party> getMaintainers() {
        return ImmutableList.of(maintainer);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return identifier;
    }

    public Party getMaintainer() {
        return maintainer;
    }

    public PublicKey getInvestorKey() {
        return investorKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UniqueIdentifier getIdentifier() {
        return identifier;
    }
}
