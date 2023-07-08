package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.exceptions.InsufficientFundsException;
import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.AppUserContact;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import com.paymybuddy.paymybuddy.repository.AppUserContactRepository;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import com.paymybuddy.paymybuddy.repository.WalletRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private  final AppUserContactRepository appUserContactRepository;
    private  final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountPayMyBuddyRepository accountPayMyBuddyRepository;



    //creat admin user for test, this  is called on startup
    @Transactional
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

            //creatAndLinkWallet(adminAppUser);
            // Create new wallet
            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.valueOf(1000000));
            wallet.setUsername(adminAppUser.getUsername());
            //Associate the wallet  with user
            adminAppUser.setWallet(wallet);
            //Associate the wallet  with user
            adminAppUser.setWallet(wallet);
            //Set the PorteMonnaie's utilisateur
            wallet.setAppUser(adminAppUser);


            //cascade setting saves the porteMonnaie as well
            return appUserRepository.save(adminAppUser);
        }
    }

    @Transactional
    public AppUser createAppUserAndWallet(AppUser appUser) {

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

    @Transactional(readOnly = true)
    public Optional<AppUser> getAppUserByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    @Transactional
    public AppUser updateAppUser(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    @Transactional
    public void deleteAppUser(int id) {
        appUserRepository.deleteById(id);
    }

    public void addContact(String userUsername, String contactUsername) {
        Optional<AppUser> userOptional = appUserRepository.findByUsername(userUsername);
        Optional<AppUser> newContactOptional = appUserRepository.findByUsername(contactUsername);

        if (userOptional.isPresent() && newContactOptional.isPresent()) {
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

    @Transactional(readOnly = true)
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

    @Transactional
    public void transferFunds (String appUserUsername, Integer contactId, BigDecimal amount){
        Optional<AppUser> appUserOptional = appUserRepository.findByUsername(appUserUsername);
        Optional<AppUser> contactOptional = appUserRepository.findById(contactId);

        if(appUserOptional.isPresent() && contactOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();
            AppUser contactToTransferTo = contactOptional.get();

            Optional<Wallet> appUserWalletOptional = walletRepository.findById(appUser.getId());
            Optional<Wallet> contactWalletOptional = walletRepository.findById(contactToTransferTo.getId());
            Optional<AccountPayMyBuddy> pmbAccountOptional = accountPayMyBuddyRepository.findById(1);

            if(appUserWalletOptional.isPresent() && contactWalletOptional.isPresent() && pmbAccountOptional.isPresent()){
                Wallet appUserWallet = appUserWalletOptional.get();
                Wallet contactToTransferToWallet = contactWalletOptional.get();
                AccountPayMyBuddy pmbAccount = pmbAccountOptional.get();

                //calculate transaction fee
                BigDecimal transactionFee = amount.multiply(BigDecimal.valueOf(pmbAccount.getTransactionFee()));
                BigDecimal finalTransactionAmount = amount.add(transactionFee);

                //check if sender balance is bigger or equal to amount plus transaction fee
                int comparisonResult = appUserWallet.getBalance().compareTo(finalTransactionAmount);
                if(comparisonResult >= 0){

                    appUserWallet.setBalance(appUserWallet.getBalance().subtract(finalTransactionAmount));
                    contactToTransferToWallet.setBalance(contactToTransferToWallet.getBalance().add(amount));
                    pmbAccount.setBalance(pmbAccount.getBalance().add(transactionFee));

                    walletRepository.save(appUserWallet);
                    walletRepository.save(contactToTransferToWallet);
                    accountPayMyBuddyRepository.save(pmbAccount);
                }else {
                    throw new InsufficientFundsException("Insufficient funds for the transfer.");
                }

            }

        }
    }

}

