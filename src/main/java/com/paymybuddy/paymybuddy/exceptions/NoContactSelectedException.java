package com.paymybuddy.paymybuddy.exceptions;

public class NoContactSelectedException extends RuntimeException{
    public NoContactSelectedException(){
        super("no contact selected");
    }
}
