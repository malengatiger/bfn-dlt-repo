package com.bfn.states;

import com.bfn.contracts.SupplierContract;
import com.bfn.schemas.SupplierSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(SupplierContract.class)
public class CustomerState implements ContractState, QueryableState {

    private String name, email, cellphone, fcmToken;
    private String sectors;
    private Date dateRegistered;
    private Party party;

    public CustomerState(Party party, String name, String email, String cellphone, String fcmToken, String sectors) {
        this.name = name;
        this.email = email;
        this.cellphone = cellphone;
        this.fcmToken = fcmToken;
        this.sectors = sectors;
        this.party = party;
    }

    public Party getParty() {
        return party;
    }

    public String getName() {
        return name;
    }

    public String getSectors() {
        return sectors;
    }

    public String getEmail() {
        return email;
    }

    public String getCellphone() {
        return cellphone;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    @Override
    public List<AbstractParty> getParticipants() {

        return Arrays.asList(party);
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if (schema instanceof SupplierSchemaV1) {
            return new SupplierSchemaV1.PersistentIOU(
                    this.name,this.email,
                    this.cellphone, this.fcmToken,this.sectors, this.dateRegistered);
        } else {
            throw new IllegalArgumentException("Object fucked");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new SupplierSchemaV1());
    }
}
