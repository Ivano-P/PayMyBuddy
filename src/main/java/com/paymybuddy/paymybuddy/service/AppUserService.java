package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;

import java.util.List;
import java.util.Optional;

public interface AppUserService {

    List<AppUser> getAllAppUsers();
    Optional<AppUser> getAppUserById(int id);
    Optional<AppUser> getAppUserByEmail(String email);
    Optional<AppUser> getAppUserByUsername(String username);
    void updateAppUser(AppUser appUser);
    void deleteAppUser(int id);
    void creatMainAdminAppUser();
    AppUser createAppUser(AppUser appUser);
    void addContact(String userUsername, String contactUsername);
    List<AppUser> getContactsForUser(AppUser user);
    void removeContact(String appUserUsername, Integer contactId);
    void checkIfAllUserInfoPresent(AppUser appUser);
    void updateUserPassword(AppUser appUser, String currentPassword,
                            String newPassword, String confirmedPassword);
}
