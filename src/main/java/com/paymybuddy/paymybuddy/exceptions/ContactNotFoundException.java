package com.paymybuddy.paymybuddy.exceptions;

public class ContactNotFoundException extends RuntimeException{

    public ContactNotFoundException(){
        super("No contact found with that username");
    }
}
