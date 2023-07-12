package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.exceptions.AccountMustBeToUsersNameException;
import com.paymybuddy.paymybuddy.exceptions.InsufficientFundsException;
import com.paymybuddy.paymybuddy.exceptions.InvalidIbanException;
import com.paymybuddy.paymybuddy.model.*;
import com.paymybuddy.paymybuddy.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Log4j2
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private  final AppUserContactRepository appUserContactRepository;
    private  final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountPayMyBuddyRepository accountPayMyBuddyRepository;
    private final AppPmbService appPmbService;
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;



    //creat admin user for test, this  is called on startup
    public AppUser creatAdminAppUser(){

        //check if first user (ADMIN) is created, if not in db creat it
        Optional<AppUser> AppUserCheck = appUserRepository.findById(1);
        if(AppUserCheck.isPresent()){
            return null;

        }else {
            AppUser adminAppUser = new AppUser();
            adminAppUser.setLastName("mister");
            adminAppUser.setFirstName("tester");
            adminAppUser.setUsername("mainadmin");
            adminAppUser.setEmail("mistertester@testmail.com");
            adminAppUser.setRole(AppUser.Role.ADMIN);
            adminAppUser.setPassword(passwordEncoder.encode("Testpassword123*"));

            setAdminUserWalletBalance(adminAppUser);

            //cascade setting saves the porteMonnaie as well
            return appUserRepository.save(adminAppUser);
        }
    }

    //set admin user balance for test. add 1 000 000 to admin wallet
    private void setAdminUserWalletBalance(AppUser appUser){
        // Create new wallet
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(1000000));
        wallet.setUsername(appUser.getUsername());
        //Associate the wallet  with user
        appUser.setWallet(wallet);
        //Associate the wallet  with user
        appUser.setWallet(wallet);
        //Set the PorteMonnaie's utilisateur
        wallet.setAppUser(appUser);

    }


    public AppUser createAppUser(AppUser appUser) {

        //Encode the password
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        //appUser.setRole(AppUser.Role.USER);

        creatAndLinkWallet(appUser);

        //cascade setting saves the wallet as well
        return appUserRepository.save(appUser);
    }

    protected void creatAndLinkWallet(AppUser appUser){
        // Create new wallet
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUsername(appUser.getUsername());
        //Associate the wallet  with user
        appUser.setWallet(wallet);
        //Associate the wallet  with user
        appUser.setWallet(wallet);
        //Set the PorteMonnaie's utilisateur
        wallet.setAppUser(appUser);
    }

    @Transactional(readOnly = true)
    public List<AppUser> getAllAppUsers() {
        return appUserRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> getAppUserById(int id) {
        return appUserRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> getAppUserByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public Optional<AppUser> getAppUserByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    public AppUser updateAppUser(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    public void deleteAppUser(int id) {
        appUserRepository.deleteById(id);
    }

    public void addContact(String userUsername, String contactUsername) {
        Optional<AppUser> userOptional = appUserRepository.findByUsername(userUsername);
        Optional<AppUser> newContactOptional = appUserRepository.findByUsername(contactUsername);

        if(newContactOptional.isEmpty()){
            throw new NoSuchElementException();
        }else if (userOptional.isPresent()) {
            AppUser user = userOptional.get();
            AppUser newContact = newContactOptional.get();

            AppUserContact.AppUserContactId id = new AppUserContact.AppUserContactId();
            id.setAppUserId(user.getId());
            id.setContactId(newContact.getId());

            AppUserContact appUserContact = new AppUserContact();
            appUserContact.setId(id);
            appUserContact.setAppUser(user);
            appUserContact.setContact(newContact);

             appUserContactRepository.save(appUserContact);

        }

    }

    public List<AppUser> getContactsForUser(AppUser user) {
        // Fetch the AppUserContact instances for the given user
        List<AppUserContact> userContacts = appUserContactRepository.findByAppUser(user);

        // Map the list of AppUserContact instances to a list of AppUser instances
        List<AppUser> contacts = userContacts.stream()
                .map(AppUserContact::getContact)
                .collect(Collectors.toList());

        return contacts;
    }

    public void removeContact(String appUserUsername, Integer contactId){
        Optional<AppUser> appUser = appUserRepository.findByUsername(appUserUsername);
        Optional<AppUser> contactToRemove = appUserRepository.findById(contactId);

        //check if there is a row in AppUserContact with this AppUserid and contactId, if so remove it
        if(appUser.isPresent() && contactToRemove.isPresent()) {
            AppUserContact.AppUserContactId id = new AppUserContact.AppUserContactId();
            id.setAppUserId(appUser.get().getId());
            id.setContactId(contactToRemove.get().getId());

            if (appUserContactRepository.existsById(id)) {
                appUserContactRepository.deleteById(id);
            }
        }

    }

    public void transferFunds (String appUserUsername, Integer contactId, BigDecimal amount, String description){
        Optional<AppUser> appUserOptional = appUserRepository.findByUsername(appUserUsername);
        Optional<AppUser> contactOptional = appUserRepository.findById(contactId);

        if(appUserOptional.isPresent() && contactOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();
            AppUser contactToTransferTo = contactOptional.get();

            Optional<Wallet> appUserWalletOptional = walletRepository.findById(appUser.getId());
            Optional<Wallet> contactWalletOptional = walletRepository.findById(contactToTransferTo.getId());
            Optional<AccountPayMyBuddy> pmbAccountOptional = accountPayMyBuddyRepository.findById(1);

            if(appUserWalletOptional.isPresent() && contactWalletOptional.isPresent() && pmbAccountOptional
                    .isPresent()){
                Wallet appUserWallet = appUserWalletOptional.get();
                Wallet recepientAppUserWallet = contactWalletOptional.get();
                AccountPayMyBuddy pmbAccount = pmbAccountOptional.get();

                //calculate transaction fee
                BigDecimal transactionFee = amount.multiply(BigDecimal.valueOf(pmbAccount.getTransactionFee()));
                BigDecimal finalTransactionAmount = amount.add(transactionFee);

                //check if sender balance is bigger or equal to amount plus transaction fee
                int comparisonResult = appUserWallet.getBalance().compareTo(finalTransactionAmount);
                if(comparisonResult >= 0){

                    appUserWallet.setBalance(appUserWallet.getBalance().subtract(finalTransactionAmount));
                    recepientAppUserWallet.setBalance(recepientAppUserWallet.getBalance().add(amount));
                    pmbAccount.setBalance(pmbAccount.getBalance().add(transactionFee));

                    walletRepository.save(appUserWallet);
                    walletRepository.save(recepientAppUserWallet);
                    accountPayMyBuddyRepository.save(pmbAccount);

                    if(description.isEmpty()){
                        appPmbService.saveTransaction(appUserWallet.getId(), recepientAppUserWallet.getId(),
                                amount, transactionFee, Transaction.TransactionType.send, Optional.empty());
                    }else{
                        appPmbService.saveTransaction(appUserWallet.getId(), recepientAppUserWallet.getId(),
                                amount, transactionFee, Transaction.TransactionType.send, description
                                        .describeConstable());
                    }


                }else {
                    throw new InsufficientFundsException("Insufficient funds for the transfer.");
                }

            }
        }
    }


    public List<TransactionForAppUserHistory> getTransactionHistory(String username){
        Optional<AppUser> appUserOptional = appUserRepository.findByUsername(username);
        int appUserId;
        if(appUserOptional.isPresent()){
            appUserId = appUserOptional.get().getId();
        }else {
            appUserId = 0;
            log.error("AppUser not found");
            //TODO: figure out what to do it app user not found
        }


        List<Transaction> appUSertransactions = transactionRepository
                .findBySenderIdOrRecepientId(appUserId,appUserId);

        List<TransactionForAppUserHistory> transactionsHistory = new ArrayList<>();

        if(!appUSertransactions.isEmpty()){
            //creat one AppUserTransactionHistory for each transaction in db.
            for(Transaction transaction : appUSertransactions){

                //find contactId in transaction
                int contactId;
                if(appUserId == transaction.getSenderId()){
                    contactId = transaction.getRecepientId();

                    //find the contactAppUSer in db
                    Optional<AppUser> contactAppUserOptionnal = appUserRepository
                            .findById(contactId);

                    //add each TransactionForAppUserHistory to transactionsHistory
                    if (contactAppUserOptionnal.isPresent()){
                        AppUser contact = contactAppUserOptionnal.get();

                        TransactionForAppUserHistory simplifiedTransaction = new TransactionForAppUserHistory(contact
                                .getUsername(), transaction.getDescription(), transaction.getAmount().negate(),
                                Transaction.TransactionType.send);
                        transactionsHistory.add(simplifiedTransaction);
                    }

                }else {
                    contactId = transaction.getSenderId();

                    //find the contactAppUSer in db
                    Optional<AppUser> contactAppUserOptionnal = appUserRepository
                            .findById(contactId);

                    //add each TransactionForAppUserHistory to transactionsHistory
                    if (contactAppUserOptionnal.isPresent()){
                        AppUser contact = contactAppUserOptionnal.get();

                        TransactionForAppUserHistory simplifiedTransaction = new TransactionForAppUserHistory(contact
                                .getUsername(), transaction.getDescription(), transaction.getAmount(),
                                Transaction.TransactionType.receive);
                        transactionsHistory.add(simplifiedTransaction);
                    }

                }

            }

        }

        return transactionsHistory;
    }

    public BankAccount checkBankAccountValidity(String username, String lasName,
                                                String firstName, String iban){
        BankAccount bankAccountToAdd = new BankAccount();
        Optional<AppUser> appUserOptional = getAppUserByUsername(username);
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


    public void addOrUpdateBankAccount(BankAccount bankAccount){

        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(bankAccount.getId());

        if(bankAccountOptional.isEmpty()){
            bankAccountRepository.save(bankAccount);
        }else{
            bankAccountRepository.deleteById(bankAccount.getId());
            bankAccountRepository.save(bankAccount);
        }
    }


    public boolean hasBankAccount(String username) {
        Optional<AppUser> appUser = getAppUserByUsername(username);
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

    public void removeBankAccount(int appUserId){
            bankAccountRepository.deleteById(appUserId);
    }

}

