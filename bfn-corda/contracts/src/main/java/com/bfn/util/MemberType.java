package com.bfn.util;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public enum MemberType {
    SUPPLIER,
    INVESTOR,
    CUSTOMER,
    BNO_STAFF;
}

