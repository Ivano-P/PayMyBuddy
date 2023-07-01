package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
}
