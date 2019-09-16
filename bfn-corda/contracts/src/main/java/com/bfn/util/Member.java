package com.bfn.util;

import net.corda.core.serialization.CordaSerializable;

import java.util.Date;

@CordaSerializable
public class Member {
    private String name, email, cellphone, memberId;
    private MemberType memberType;
    private Date dateRegistered;

    public Member() {
    }

    public Member(String name, String email, String cellphone, String memberId, MemberType memberType) {
        this.name = name;
        this.email = email;
        this.cellphone = cellphone;
        this.memberId = memberId;
        this.memberType = memberType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(MemberType memberType) {
        this.memberType = memberType;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }
}
