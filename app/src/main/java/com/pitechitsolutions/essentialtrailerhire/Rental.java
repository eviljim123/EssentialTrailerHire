package com.pitechitsolutions.essentialtrailerhire;

public class Rental {
    private String branchID;
    private String rentalDateTime;
    private String selectedDeliveryDateTime;
    private Boolean termsAndConditionsAccepted;
    private String clientSignature;
    private String bookingStatus;
    private double charge;
    private boolean oneWayTrip;
    private String customerID;
    private String trailerID;
    private String deliveryDateTime;  // Added this attribute
    private String signature;  // Added this attribute

    public Rental() {
    }

    public String getBranchID() {
        return branchID;
    }

    public void setBranchID(String branchID) {
        this.branchID = branchID;
    }

    public String getRentalDateTime() {
        return rentalDateTime;
    }

    public void setRentalDateTime(String rentalDateTime) {
        this.rentalDateTime = rentalDateTime;
    }

    public String getSelectedDeliveryDateTime() {
        return selectedDeliveryDateTime;
    }

    public void setSelectedDeliveryDateTime(String selectedDeliveryDateTime) {
        this.selectedDeliveryDateTime = selectedDeliveryDateTime;
    }

    public Boolean getTermsAndConditionsAccepted() {
        return termsAndConditionsAccepted;
    }

    public void setTermsAndConditionsAccepted(Boolean termsAndConditionsAccepted) {
        this.termsAndConditionsAccepted = termsAndConditionsAccepted;
    }

    public String getClientSignature() {
        return clientSignature;
    }

    public void setClientSignature(String clientSignature) {
        this.clientSignature = clientSignature;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public boolean isOneWayTrip() {
        return oneWayTrip;
    }

    public void setOneWayTrip(boolean oneWayTrip) {
        this.oneWayTrip = oneWayTrip;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getTrailerID() {
        return trailerID;
    }

    public void setTrailerID(String trailerID) {
        this.trailerID = trailerID;
    }

    public void setDeliveryDateTime(String deliveryDateTime) {
        this.deliveryDateTime = deliveryDateTime;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
