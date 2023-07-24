package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.BankAccount;

public interface BankAccountService {
    void removeBankAccount(int appUserId);
    void updateBankAccount(BankAccount bankAccount);
    BankAccount checkBankAccountValidity(String username, String lasName,
                                         String firstName, String iban);
    boolean hasBankAccount(String username);
    BankAccount getAppUserBankAccount(int currentAppUserId);
    void noBankAccountForWithdrawal();
}
