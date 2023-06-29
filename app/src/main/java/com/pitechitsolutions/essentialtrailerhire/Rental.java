package com.pitechitsolutions.essentialtrailerhire;

public class Rental {
    private String rentalId;
    private String invoiceNumber;
    private String driverLicenseUrl;
    private String vehicleDiskUrl;
    private String signatureUrl;
    private String bookingStatus;
    private String branchId;
    private String b2bInvoiceNumber;
    private String clientSignature;
    private String customerId;
    private boolean oneWayTrip;
    private String rentalDateTime;
    private String selectedDeliveryDateTime;
    private boolean termsAndConditionsAccepted;
    private String trailerId;
    private String trailerBarcode;
    private String currentLocation;
    private String deliveryDestination;
    private String trailerRemarks;
    private String customerName;
    private String customerSurname;
    private String customerContactNumber;


    public Rental() {
        // Default constructor required for calls to DataSnapshot.getValue(Rental.class)
    }

    // Add all-args constructor and getter/setter methods below

    public Rental(String rentalId, String invoiceNumber, String driverLicenseUrl, String vehicleDiskUrl, String signatureUrl, String bookingStatus, String branchId, String b2bInvoiceNumber, String clientSignature, String customerId, boolean oneWayTrip, String rentalDateTime, String selectedDeliveryDateTime, boolean termsAndConditionsAccepted, String trailerId, String trailerBarcode, String currentLocation, String deliveryDestination, String trailerRemarks) {
        this.rentalId = rentalId;
        this.invoiceNumber = invoiceNumber;
        this.driverLicenseUrl = driverLicenseUrl;
        this.vehicleDiskUrl = vehicleDiskUrl;
        this.signatureUrl = signatureUrl;
        this.bookingStatus = bookingStatus;
        this.branchId = branchId;
        this.b2bInvoiceNumber = b2bInvoiceNumber;
        this.clientSignature = clientSignature;
        this.customerId = customerId;
        this.oneWayTrip = oneWayTrip;
        this.rentalDateTime = rentalDateTime;
        this.selectedDeliveryDateTime = selectedDeliveryDateTime;
        this.termsAndConditionsAccepted = termsAndConditionsAccepted;
        this.trailerId = trailerId;
        this.trailerBarcode = trailerBarcode;
        this.currentLocation = currentLocation;
        this.deliveryDestination = deliveryDestination;
        this.trailerRemarks = trailerRemarks;

    }

    // Getter and Setter methods

    public String getTrailerBarcode() {
        return trailerBarcode;
    }

    public void setTrailerBarcode(String trailerBarcode) {
        this.trailerBarcode = trailerBarcode;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getDeliveryDestination() {
        return deliveryDestination;
    }

    public void setDeliveryDestination(String deliveryDestination) {
        this.deliveryDestination = deliveryDestination;
    }

    public String getTrailerRemarks() {
        return trailerRemarks;
    }

    public void setTrailerRemarks(String trailerRemarks) {
        this.trailerRemarks = trailerRemarks;
    }
    public String getRentalId() {
        return rentalId;
    }

    public void setRentalId(String rentalId) {
        this.rentalId = rentalId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDriverLicenseUrl() {
        return driverLicenseUrl;
    }

    public void setDriverLicenseUrl(String driverLicenseUrl) {
        this.driverLicenseUrl = driverLicenseUrl;
    }

    public String getVehicleDiskUrl() {
        return vehicleDiskUrl;
    }

    public void setVehicleDiskUrl(String vehicleDiskUrl) {
        this.vehicleDiskUrl = vehicleDiskUrl;
    }

    public String getSignatureUrl() {
        return signatureUrl;
    }

    public void setSignatureUrl(String signatureUrl) {
        this.signatureUrl = signatureUrl;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getB2bInvoiceNumber() {
        return b2bInvoiceNumber;
    }

    public void setB2bInvoiceNumber(String b2bInvoiceNumber) {
        this.b2bInvoiceNumber = b2bInvoiceNumber;
    }


    public String getClientSignature() {
        return clientSignature;
    }

    public void setClientSignature(String clientSignature) {
        this.clientSignature = clientSignature;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public boolean isOneWayTrip() {
        return oneWayTrip;
    }

    public void setOneWayTrip(boolean oneWayTrip) {
        this.oneWayTrip = oneWayTrip;
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

    public boolean isTermsAndConditionsAccepted() {
        return termsAndConditionsAccepted;
    }

    public void setTermsAndConditionsAccepted(boolean termsAndConditionsAccepted) {
        this.termsAndConditionsAccepted = termsAndConditionsAccepted;
    }

    public String getTrailerId() {
        return trailerId;
    }

    public void setTrailerId(String trailerId) {
        this.trailerId = trailerId;
    }
}