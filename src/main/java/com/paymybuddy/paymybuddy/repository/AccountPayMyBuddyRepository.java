package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountPayMyBuddyRepository extends JpaRepository<AccountPayMyBuddy, Integer> {
}
