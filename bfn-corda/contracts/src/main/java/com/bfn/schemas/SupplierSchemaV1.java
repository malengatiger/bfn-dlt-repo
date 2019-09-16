package com.bfn.schemas;

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.PersistentStateRef;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * An SupplierSchema schema.
 */
public class SupplierSchemaV1 extends MappedSchema {
    public SupplierSchemaV1() {
        super(SupplierSchema.class, 1, ImmutableList.of(PersistentIOU.class));
    }

    @Entity
    @Table(name = "supplier_states")
    public static class PersistentIOU extends PersistentState {
        @Column(name = "name") private final String name;
        @Column(name = "email") private final String email;
        @Column(name = "cellphone") private final String cellphone;
        @Column(name = "fcmToken") private final String fcmToken;
        @Column(name = "sectors") private final String sectors;
        @Column(name = "dateRegistered") private final Date dateRegistered;


        public PersistentIOU(@Nullable PersistentStateRef stateRef, String name, String email, String cellphone, String fcmToken, String sectors, Date dateRegistered) {
            super(stateRef);
            this.name = name;
            this.email = email;
            this.cellphone = cellphone;
            this.fcmToken = fcmToken;
            this.sectors = sectors;
            this.dateRegistered = dateRegistered;
        }

        public PersistentIOU(String name, String email, String cellphone, String fcmToken, String sectors, Date dateRegistered) {
            this.name = name;
            this.email = email;
            this.cellphone = cellphone;
            this.fcmToken = fcmToken;
            this.sectors = sectors;
            this.dateRegistered = dateRegistered;
        }

        public String getName() {
            return name;
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

        public String getSectors() {
            return sectors;
        }

        public Date getDateRegistered() {
            return dateRegistered;
        }
    }
}
