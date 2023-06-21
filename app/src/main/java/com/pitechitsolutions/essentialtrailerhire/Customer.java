package com.pitechitsolutions.essentialtrailerhire;

public class Customer {
    public String name;
    public String surname;
    public String idNumber;
    public String contactNumber;
    public String email;
    public String driversLicensePhoto;
    public String vehicleDiskPhoto;
    public String residentialAddress;
    public String b2bInvoiceNumber;
    private String invoiceNumber;



    public Customer() {
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDriversLicensePhoto() {
        return driversLicensePhoto;
    }

    public void setDriversLicensePhoto(String driversLicensePhoto) {
        this.driversLicensePhoto = driversLicensePhoto;
    }

    public String getVehicleDiskPhoto() {
        return vehicleDiskPhoto;
    }

    public void setVehicleDiskPhoto(String vehicleDiskPhoto) {
        this.vehicleDiskPhoto = vehicleDiskPhoto;
    }

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public void setResidentialAddress(String residentialAddress) {
        this.residentialAddress = residentialAddress;
    }

    public String getB2bInvoiceNumber() {
        return b2bInvoiceNumber;
    }

    public void setB2bInvoiceNumber(String b2bInvoiceNumber) {
        this.b2bInvoiceNumber = b2bInvoiceNumber;
    }
}
