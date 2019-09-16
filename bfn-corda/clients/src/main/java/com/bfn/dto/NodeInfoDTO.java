package com.bfn.dto;

import java.util.List;

public class NodeInfoDTO {
    List addresses;
    int  platformVersion;
    long serial;

    public List getAddresses() {
        return addresses;
    }

    public void setAddresses(List addresses) {
        this.addresses = addresses;
    }


    public int getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(int platformVersion) {
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
