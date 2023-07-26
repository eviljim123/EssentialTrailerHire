package com.pitechitsolutions.essentialtrailerhire;

public class OutstandingRental {
    private long additionalCharge;
    private String customerId;
    private long overdueTimeInHours;
    private String s2sInvoiceNumber;
    private String deliveryDestination;
    private String returnDate; // new field

    // Default constructor required for calls to DataSnapshot.getValue(OutstandingRental.class)
    public OutstandingRental() {}

    public OutstandingRental(long additionalCharge, String customerId, long overdueTimeInHours,
                             String s2sInvoiceNumber, String deliveryDestination, String returnDate) { // updated constructor
        this.additionalCharge = additionalCharge;
        this.customerId = customerId;
        this.overdueTimeInHours = overdueTimeInHours;
        this.s2sInvoiceNumber = s2sInvoiceNumber;
        this.deliveryDestination = deliveryDestination;
        this.returnDate = returnDate; // set the new field
    }

    public long getAdditionalCharge() {
        return additionalCharge;
    }

    public void setAdditionalCharge(long additionalCharge) {
        this.additionalCharge = additionalCharge;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public long getOverdueTimeInHours() {
        return overdueTimeInHours;
    }

    public void setOverdueTimeInHours(long overdueTimeInHours) {
        this.overdueTimeInHours = overdueTimeInHours;
    }

    public String getS2sInvoiceNumber() {
        return s2sInvoiceNumber;
    }

    public void setS2sInvoiceNumber(String s2sInvoiceNumber) {
        this.s2sInvoiceNumber = s2sInvoiceNumber;
    }

    public String getDeliveryDestination() {
        return deliveryDestination;
    }

    public void setDeliveryDestination(String deliveryDestination) {
        this.deliveryDestination = deliveryDestination;
    }

    // New getter and setter for the returnDate field
    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }
}
