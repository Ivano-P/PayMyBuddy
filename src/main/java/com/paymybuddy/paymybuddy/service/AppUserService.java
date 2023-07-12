package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.exceptions.AccountMustBeToUsersNameException;
import com.paymybuddy.paymybuddy.exceptions.InsufficientFundsException;
import com.paymybuddy.paymybuddy.exceptions.InvalidIbanException;
import com.paymybuddy.paymybuddy.exceptions.NoBankAccountException;
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





}

