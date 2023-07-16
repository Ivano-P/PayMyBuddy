package com.paymybuddy.paymybuddy.exceptions;

public class InvalidAmountException extends RuntimeException{
    public InvalidAmountException() {super("Amount must be between 1€ and 10 000€"); }
}
