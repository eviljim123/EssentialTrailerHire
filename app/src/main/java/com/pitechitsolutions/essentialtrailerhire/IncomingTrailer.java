package com.pitechitsolutions.essentialtrailerhire;

public class IncomingTrailer {
    private String branchID;
    private String deliveryBranch;
    private String estimatedArrivalDateTime;
    private String originBranch;
    private String status;
    private String trailerId;

    public IncomingTrailer() {
        // Default constructor required for calls to DataSnapshot.getValue(IncomingTrailer.class)
    }

    public IncomingTrailer(String branchID, String deliveryBranch, String estimatedArrivalDateTime, String originBranch, String status, String trailerId) {
        this.branchID = branchID;
        this.deliveryBranch = deliveryBranch;
        this.estimatedArrivalDateTime = estimatedArrivalDateTime;
        this.originBranch = originBranch;
        this.status = status;
        this.trailerId = trailerId;
    }

    public String getBranchID() {
        return branchID;
    }

    public void setBranchID(String branchID) {
        this.branchID = branchID;
    }

    public String getDeliveryBranch() {
        return deliveryBranch;
    }

    public void setDeliveryBranch(String deliveryBranch) {
        this.deliveryBranch = deliveryBranch;
    }

    public String getEstimatedArrivalDateTime() {
        return estimatedArrivalDateTime;
    }

    public void setEstimatedArrivalDateTime(String estimatedArrivalDateTime) {
        this.estimatedArrivalDateTime = estimatedArrivalDateTime;
    }

    public String getOriginBranch() {
        return originBranch;
    }

    public void setOriginBranch(String originBranch) {
        this.originBranch = originBranch;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrailerId() {
        return trailerId;
    }

    public void setTrailerId(String trailerId) {
        this.trailerId = trailerId;
    }

    @Override
    public String toString() {
        return "Incoming Trailer from " + originBranch +
                "\nEstimated Arrival Time: " + estimatedArrivalDateTime;
    }
}
