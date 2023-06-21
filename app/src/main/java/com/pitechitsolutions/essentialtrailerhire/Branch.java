package com.pitechitsolutions.essentialtrailerhire;

import java.util.HashMap;
import java.util.Map;

public class Branch {
    public String branchName;
    public String branchPassword;
    public String branchCoordinates;
    public Map<String, String> tradingHours;

    public Branch() {
    }

    public Branch(String branchName, String branchPassword, String branchCoordinates) {
        this.branchName = branchName;
        this.branchPassword = branchPassword;
        this.branchCoordinates = branchCoordinates;
        this.tradingHours = new HashMap<>();
        this.tradingHours.put("start", "07:00");
        this.tradingHours.put("end", "17:00");
    }
}
