package com.bfn.dto;

public class AccountInfoDTO {
    String identifier, host, name, status;

    public AccountInfoDTO(String identifier, String host, String name, String status) {
        this.identifier = identifier;
        this.host = host;
        this.name = name;
        this.status = status;
    }

    public AccountInfoDTO() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
