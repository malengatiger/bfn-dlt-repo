package com.bfn.dto;

import java.util.List;

public class NodeInfoDTO {
    List addresses;
    long  platformVersion;
    long serial;
    String webAPIUrl;


    public List getAddresses() {
        return addresses;
    }

    public void setAddresses(List addresses) {
        this.addresses = addresses;
    }

    public String getWebAPIUrl() {
        return webAPIUrl;
    }

    public void setWebAPIUrl(String webAPIUrl) {
        this.webAPIUrl = webAPIUrl;
    }

    public long getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(long platformVersion) {
        this.platformVersion = platformVersion;
    }

    public long getSerial() {
        return serial;
    }

    public void setSerial(long serial) {
        this.serial = serial;
    }

    public NodeInfoDTO() {
    }
}
