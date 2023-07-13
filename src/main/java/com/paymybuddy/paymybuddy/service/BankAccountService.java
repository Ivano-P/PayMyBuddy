package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.exceptions.AccountMustBeToUsersNameException;
import com.paymybuddy.paymybuddy.exceptions.InvalidIbanException;
import com.paymybuddy.paymybuddy.exceptions.NoBankAccountException;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.repository.BankAccountRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Log4j2
@Transactional
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final AppUserService appUserService;


    public void removeBankAccount(int appUserId){
        bankAccountRepository.deleteById(appUserId);
    }

    public void updateBankAccount(BankAccount bankAccount){

        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(bankAccount.getId());

        if(bankAccountOptional.isEmpty()){
            bankAccountRepository.save(bankAccount);
        }else{
            bankAccountRepository.deleteById(bankAccount.getId());
            bankAccountRepository.save(bankAccount);
        }
    }


    public BankAccount checkBankAccountValidity(String username, String lasName,
                                                String firstName, String iban){
        BankAccount bankAccountToAdd = new BankAccount();
        Optional<AppUser> appUserOptional = appUserService.getAppUserByUsername(username);
        if(appUserOptional.isPresent()){
            AppUser appUserToCompare = appUserOptional.get();

            //check that name entered is the same as username on app.
            if (lasName.equals(appUserToCompare.getLastName()) && firstName.equals(appUserToCompare.getFirstName())){
                String fullName = lasName + " " + firstName;
                int ibanLenght = iban.length();
                if(ibanLenght > 21 && ibanLenght < 34){
                    bankAccountToAdd = new BankAccount(appUserToCompare.getId(), fullName, iban);
                }else {
                    log.error("Invalid Iban Exception");
                    throw new InvalidIbanException("Invalid Iban, Iban must be between 22 and 34 characters");
                }
            }else{
                log.debug("AccountMustBeToUsersNameException");
                throw new AccountMustBeToUsersNameException("Account must be to user's name");
            }
        }
        log.debug(bankAccountToAdd);
        return bankAccountToAdd;

    }


    public boolean hasBankAccount(String username) {
        Optional<AppUser> appUser = appUserService.getAppUserByUsername(username);
        boolean hasBankAccount = false;
        if (appUser.isPresent()) {
            Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(appUser.get().getId());
            if(bankAccountOptional.isPresent()) {
                hasBankAccount = true;
            }
        }

        log.debug(hasBankAccount);
        return hasBankAccount;
    }

    public BankAccount getAppUserBankAccount(int currentAppUserId){
        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(currentAppUserId);
        BankAccount currentBankAccount = new BankAccount();

        if (bankAccountOptional.isPresent()){
            currentBankAccount = bankAccountOptional.get();
        }else {
            log.error("Error with saved bank account in db");
        }

        return currentBankAccount;
    }



    public boolean showIbanForDeposit(boolean showIban){
        return showIban = true;
    }

    public void noBankAccountForWithdrawal(){
        throw new NoBankAccountException("Withdrawal not possible, no bank account added");
    }


}
