package com.paymybuddy.paymybuddy.implementation;

import com.paymybuddy.paymybuddy.exceptions.ContactNotFoundException;
import com.paymybuddy.paymybuddy.exceptions.InvalidPasswordException;
import com.paymybuddy.paymybuddy.exceptions.MissingUserInfoException;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.AppUserContact;
import com.paymybuddy.paymybuddy.repository.AppUserContactRepository;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import com.paymybuddy.paymybuddy.service.AppUserService;
import com.paymybuddy.paymybuddy.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Log4j2
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserContactRepository appUserContactRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    public List<AppUser> getAllAppUsers() {
        log.info("getAllAppUsers method called");
        return appUserRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> getAppUserById(int id) {
        log.info("getAppUserById method called with : {}", id);
        return appUserRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> getAppUserByEmail(String email) {
        log.info("getAppUserByEmail method called with : {}", email);
        return appUserRepository.findByEmail(email);
    }

    public Optional<AppUser> getAppUserByUsername(String username) {
        log.info("getAppUserByUsername method called with : {}", username);
        return appUserRepository.findByUsername(username);
    }

    public void updateAppUser(AppUser appUser) {
        log.info("updateAppUser method called with : {}", appUser);
        appUserRepository.save(appUser);
    }

    public void deleteAppUser(int id) {
        log.info("deleteAppUser method called with : {}", id);
        appUserRepository.deleteById(id);
    }


    //creat admin user for test, this  is called on startup
    public void creatMainAdminAppUser() {
        log.info("creatMainAdminAppUser method called");
        //check if first user (ADMIN) is created, if not in db creat it
        Optional<AppUser> appUserCheck = appUserRepository.findById(1);
        if (appUserCheck.isEmpty()) {
            AppUser adminAppUser = new AppUser();
            adminAppUser.setLastName("mister");
            adminAppUser.setFirstName("tester");
            adminAppUser.setUsername("mainadmin");
            adminAppUser.setEmail("mistertester@testmail.com");
            adminAppUser.setRole(AppUser.Role.ADMIN);
            adminAppUser.setPassword(passwordEncoder.encode(System.getenv("PMB_MAINADMIN_PASSWORD")));

            walletService.setAdminUserWalletBalance(adminAppUser);

            //cascade setting saves the wallet as well
            appUserRepository.save(adminAppUser);

        }
    }


    public AppUser createAppUser(AppUser appUser) {
        log.info("createAppUser method called with : {}", appUser);

        //Encode the password
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));

        walletService.creatAndLinkWallet(appUser);

        //cascade setting saves the wallet as well
        return appUserRepository.save(appUser);
    }


    public void addContact(String userUsername, String contactUsername) {
        log.info("addContact method called with : {} and {}", userUsername, contactUsername);
        Optional<AppUser> userOptional = appUserRepository.findByUsername(userUsername);
        Optional<AppUser> newContactOptional = appUserRepository.findByUsername(contactUsername);

        if (newContactOptional.isEmpty()) {
            throw new ContactNotFoundException();
        } else if (userOptional.isPresent()) {
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
            log.info("Successfully added contact {} for user {}", contactUsername, userUsername);
        } else {
            log.warn("User {} not found when trying to add contact {}", userUsername, contactUsername);
        }

    }

    public List<AppUser> getContactsForUser(AppUser user) {
        log.info("addContact method called with : {}", user);
        // Fetch the AppUserContact instances for the given user
        List<AppUserContact> userContacts = appUserContactRepository.findByAppUser(user);

        // Map the list of AppUserContact instances to a list of AppUser instances

        return userContacts.stream()
                .map(AppUserContact::getContact)
                .toList();
    }

    public void removeContact(String appUserUsername, Integer contactId) {
        log.info("removeContact method called with : {} and {}", appUserUsername, contactId);
        Optional<AppUser> appUser = appUserRepository.findByUsername(appUserUsername);
        Optional<AppUser> contactToRemove = appUserRepository.findById(contactId);

        //check if there is a row in AppUserContact with this AppUserid and contactId, if so remove it
        if (appUser.isPresent() && contactToRemove.isPresent()) {
            AppUserContact.AppUserContactId id = new AppUserContact.AppUserContactId();
            id.setAppUserId(appUser.get().getId());
            id.setContactId(contactToRemove.get().getId());

            if (appUserContactRepository.existsById(id)) {
                appUserContactRepository.deleteById(id);
            }
        }

    }

    public void checkIfAllUserInfoPresent(AppUser appUser) {
        log.info("checkIfAllUserInfoPresent method called with : {}", appUser);
        boolean allInfoArePresent;

        allInfoArePresent = appUser.getFirstName() != null && appUser.getLastName() != null && appUser
                .getEmail() != null;

        if (!allInfoArePresent) {
            throw new MissingUserInfoException("First name, last name and email must be registered");
        }

    }

    public void updateUserPassword(AppUser appUser, String currentPassword,
                                   String newPassword, String confirmedPassword) {
        log.info("updateUserPassword method called for : {}", appUser.getUsername());

        if (checkOldPassword(appUser, currentPassword)) {
            if (newPassword.equals(confirmedPassword)) {
                appUser.setPassword(passwordEncoder.encode(newPassword));

            } else {
                throw new InvalidPasswordException("Passwords do not match");
            }

        } else {
            throw new InvalidPasswordException("Old password is incorrect");
        }

    }

    //returns true if current password and password user imputed are the same
    private boolean checkOldPassword(AppUser currentUser, String oldPassword) {
        log.info("checkOldPassword method called with : {}", currentUser);
        return BCrypt.checkpw(oldPassword, currentUser.getPassword());
    }

}

