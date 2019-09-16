package com.bfn.dto;

public class MyCordaName {
    String organization, locality, country;

    public MyCordaName(String organization, String locality, String country) {
        this.organization = organization;
        this.locality = locality;
        this.country = country;
    }

    public MyCordaName() {
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
