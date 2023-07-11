package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Transaction findBySenderIdOrRecepientId(int senderId, int recepientId);
}
