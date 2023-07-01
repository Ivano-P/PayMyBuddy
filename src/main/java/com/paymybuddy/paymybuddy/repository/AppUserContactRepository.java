package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.AppUserContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppUserContactRepository extends JpaRepository <AppUserContact, AppUserContact.AppUserContactId> {
    List<AppUserContact> findByAppUser(AppUser appUser);
}
