package com.pitechitsolutions.essentialtrailerhire;

public class Trailer {

    private String barcode;
    private String vinNo;
    private String licensePlateNumber;
    private String condition;
    private String status;
    private String remarks;
    private String returnDate;
    private String type;  // <- Add this
    private double estimatedDistance;
    private String currentLocation;
    private boolean oneWayTrip;
    private String deliveryDestination;


    // Default constructor required for calls to DataSnapshot.getValue(Trailer.class)
    public Trailer() {
    }

    // New constructor
    public Trailer(String barcode, String condition, String remarks, String returnDate) {
        this.barcode = barcode;
        this.condition = condition;
        this.remarks = remarks;
        this.returnDate = returnDate;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getVinNo() {
        return vinNo;
    }

    public void setVinNo(String vinNo) {
        this.vinNo = vinNo;
    }

    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public void setLicensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public double getEstimatedDistance() {
        return estimatedDistance;
    }

    public void setEstimatedDistance(double estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean isOneWayTrip() {
        return oneWayTrip;
    }

    public void setOneWayTrip(boolean oneWayTrip) {
        this.oneWayTrip = oneWayTrip;
    }

    public String getDeliveryDestination() {
        return deliveryDestination;
    }

    public void setDeliveryDestination(String deliveryDestination) {
        this.deliveryDestination = deliveryDestination;
    }


}
