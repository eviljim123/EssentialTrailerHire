package com.pitechitsolutions.essentialtrailerhire;

import java.util.HashMap;
import java.util.Map;

public class Branch {
    public String branchName;
    public String branchPassword;
    public Double latitude;
    public Double longitude;
    public Map<String, String> tradingHours;

    public Branch() {
    }

    public Branch(String branchName, String branchPassword, Double latitude, Double longitude) {
        this.branchName = branchName;
        this.branchPassword = branchPassword;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tradingHours = new HashMap<>();
        this.tradingHours.put("start", "07:00");
        this.tradingHours.put("end", "17:00");
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchPassword() {
        return branchPassword;
    }

    public void setBranchPassword(String branchPassword) {
        this.branchPassword = branchPassword;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Map<String, String> getTradingHours() {
        return tradingHours;
    }

    public void setTradingHours(Map<String, String> tradingHours) {
        this.tradingHours = tradingHours;
    }
}
