package com.ocs.dynamo.jasperreports;

public class Customer {

    private String customerName;
    private String email;
    private Person primaryContact;

    public Customer(String customerName) {
	super();
	this.customerName = customerName;
    }

    public Customer(String customerName, Person primaryContact) {
	super();
	this.customerName = customerName;
	this.primaryContact = primaryContact;
    }

    public Customer(String customerName, String email, Person primaryContact) {
	super();
	this.customerName = customerName;
	this.email = email;
	this.primaryContact = primaryContact;
    }

    public String getCustomerName() {
	return customerName;
    }

    public Person getPrimaryContact() {
	return primaryContact;
    }

    public void setPrimaryContact(Person primaryContact) {
	this.primaryContact = primaryContact;
    }

    public String getEmail() {
	return email;
    }

}
