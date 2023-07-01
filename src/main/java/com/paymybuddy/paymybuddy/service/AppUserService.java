package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.AppUserContact;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.AppUserContactRepository;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
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
    private final PasswordEncoder passwordEncoder;

    //creat admin user for test, this  is called on startup
    @Transactional
    public AppUser creatAdminAppUser(){

        //check if first user (ADMIN) is created, if not in db creat it
        Optional<AppUser> userCheck = appUserRepository.findById(1);
        if(userCheck.isPresent()){
            return null;

        }else {
            AppUser adminAppUser = new AppUser();
            adminAppUser.setLastName("petty");
            adminAppUser.setFirstName("ivano");
            adminAppUser.setEmail("mistertester@testmail.com");
            adminAppUser.setRole("ADMIN");
            adminAppUser.setPassword(passwordEncoder.encode("Testpassword123*"));

            // Create new wallet
            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.valueOf(10000.00));
            wallet.setUserEmail(adminAppUser.getEmail());

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

        // Create new wallet
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUserEmail(appUser.getEmail());

        //Associate the wallet  with user
        appUser.setWallet(wallet);

        // Set the default role of the user
        appUser.setRole("USER");

        //Set the Wallets user
        wallet.setAppUser(appUser);

        //cascade setting saves the wallet as well
        return appUserRepository.save(appUser);
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

    @Transactional
    public AppUser updateAppUser(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    @Transactional
    public void deleteAppUser(int id) {
        appUserRepository.deleteById(id);
    }

    public void addContact(String userEmail, String contactEmail) {
        Optional<AppUser> userOptional = appUserRepository.findByEmail(userEmail);
        Optional<AppUser> newContactOptional = appUserRepository.findByEmail(contactEmail);

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

        } //TODO: add Exeption for if contact is not found

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

    public void removeContact(String appUserEmail, Integer contactId){
        Optional<AppUser> appUser = appUserRepository.findByEmail(appUserEmail);
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

