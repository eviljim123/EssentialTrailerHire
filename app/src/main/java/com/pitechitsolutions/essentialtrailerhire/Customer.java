package com.pitechitsolutions.essentialtrailerhire;

public class Customer {
    private String name;
    private String surname;
    private String idNumber;
    private String contactNumber;
    private String email;
    private String residentialAddress;

    public Customer() {
        // Default constructor required for calls to DataSnapshot.getValue(Customer.class)
    }

    public Customer(String name, String surname, String idNumber, String contactNumber, String email, String residentialAddress) {
        this.name = name;
        this.surname = surname;
        this.idNumber = idNumber;
        this.contactNumber = contactNumber;
        this.email = email;
        this.residentialAddress = residentialAddress;
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

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public void setResidentialAddress(String residentialAddress) {
        this.residentialAddress = residentialAddress;
    }
}
