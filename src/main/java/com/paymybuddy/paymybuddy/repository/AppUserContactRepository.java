package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.AppUserContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserContactRepository extends JpaRepository <AppUserContact, AppUserContact.AppUserContactId> {
}
